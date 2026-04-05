package com.mhsa.backend.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.ai.dto.AiChatRequest;
import com.mhsa.backend.ai.dto.AiChatResponse;
import com.mhsa.backend.ai.entity.ChatMessage;
import com.mhsa.backend.ai.entity.ChatSession;
import com.mhsa.backend.ai.repository.ChatMessageRepository;
import com.mhsa.backend.ai.repository.ChatSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ContextAggregatorService contextAggregatorService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private static final String BASE_SYSTEM_PROMPT = "Ngươi là 'Bạn Tâm Giao', một trợ lý tâm lý học thấu cảm. Luôn xưng là 'mình' và gọi người dùng là 'bạn'. Hãy tham khảo [USER CONTEXT] dưới đây để đưa ra lời khuyên cá nhân hóa, nhưng KHÔNG ĐƯỢC nhắc lại dữ liệu một cách máy móc. Hãy an ủi tự nhiên.";

    /**
     * Send a message to Gemini AI and get a response
     *
     * @param request The user's chat request
     * @return The AI response
     */
    public AiChatResponse sendMessage(AiChatRequest request, UUID profileId) {
        try {
            log.info("Sending message to Gemini API. SessionId: {}", request.getSessionId());

            // 1. Find or create ChatSession
            ChatSession session;
            if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
                session = chatSessionRepository.findById(UUID.fromString(request.getSessionId()))
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            } else {
                session = ChatSession.builder()
                        .profileId(profileId)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();
                session = chatSessionRepository.save(session);
            }

            // 2. Save user's message
            ChatMessage userMessage = ChatMessage.builder()
                    .session(session)
                    .sender("USER")
                    .content(request.getContent())
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
            chatMessageRepository.save(userMessage);

            // 3. Get last 10 messages for context
            List<ChatMessage> lastMessages = chatMessageRepository.findTop10BySessionOrderBySentAtDesc(session);
            java.util.Collections.reverse(lastMessages); // oldest first
            StringBuilder chatHistoryBuilder = new StringBuilder();
            for (ChatMessage msg : lastMessages) {
                chatHistoryBuilder.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
            }
            String chatHistory = chatHistoryBuilder.toString();

            // 4. Get user context summary
            String userContext = contextAggregatorService.getUserContextSummary(profileId);

            // 5. Compose the mega system prompt
            String systemPrompt = BASE_SYSTEM_PROMPT + "\n" + userContext;

            // 6. Build prompt: systemPrompt + chat history + user message
            StringBuilder megaPrompt = new StringBuilder();
            megaPrompt.append(systemPrompt).append("\n");
            megaPrompt.append(chatHistory);
            megaPrompt.append("USER: ").append(request.getContent());

            // 7. Build Gemini request
            Map<String, Object> requestPayload = buildGeminiRequest(megaPrompt.toString(), systemPrompt);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<String> httpEntity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestPayload),
                    headers
            );

            // Make the API request
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

            // Parse and extract response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String aiResponse = parseGeminiResponse(response.getBody());
                log.info("Successfully received response from Gemini API");

                // 8. Save AI response
                ChatMessage aiMessage = ChatMessage.builder()
                        .session(session)
                        .sender("AI")
                        .content(aiResponse)
                        .sentAt(java.time.LocalDateTime.now())
                        .build();
                chatMessageRepository.save(aiMessage);

                return AiChatResponse.builder()
                        .sessionId(session.getId().toString())
                        .messageId(aiMessage.getId().toString())
                        .content(aiResponse)
                        .sentimentDetected("NEUTRAL") // Mock sentiment for now
                        .crisisDetected(false) // Mock crisis detection for now
                        .build();
            } else {
                log.error("Gemini API returned non-success status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to get response from Gemini API");
            }

        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to communicate with Gemini API", e);
        } catch (Exception e) {
            log.error("Error processing Gemini response", e);
            throw new RuntimeException("Error processing AI response", e);
        }
    }

    /**
     * Build the request payload for Gemini API
     *
     * @param userMessage The user's message
     * @return The request payload as a Map
     */
    private Map<String, Object> buildGeminiRequest(String megaPrompt, String systemPrompt) {
        Map<String, Object> request = new HashMap<>();

        // Add system instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, String> parts = new HashMap<>();
        parts.put("text", systemPrompt);
        systemInstruction.put("parts", parts);
        request.put("system_instruction", systemInstruction);

        // Add mega prompt as user message
        List<Map<String, Object>> contents = List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("text", megaPrompt)
                        )
                )
        );
        request.put("contents", contents);

        // Add safety settings for mental health context
        List<Map<String, String>> safetySettings = List.of(
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_ONLY_HIGH")
        );
        request.put("safetySettings", safetySettings);

        // Add generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        request.put("generation_config", generationConfig);

        return request;
    }

    /**
     * Parse the response from Gemini API and extract the text content
     *
     * @param responseBody The JSON response body from Gemini
     * @return The extracted text response
     */
    private String parseGeminiResponse(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // Navigate to candidates[0].content.parts[0].text
        JsonNode candidatesNode = rootNode.path("candidates");
        if (candidatesNode.isArray() && candidatesNode.size() > 0) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");

            if (partsNode.isArray() && partsNode.size() > 0) {
                JsonNode firstPart = partsNode.get(0);
                JsonNode textNode = firstPart.path("text");

                if (textNode != null && !textNode.isNull()) {
                    return textNode.asText();
                }
            }
        }

        throw new RuntimeException("Unable to parse Gemini API response");
    }
}
