package com.mhsa.backend.ai.service;

import java.util.Collections;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.ai.dto.AiChatRequest;
import com.mhsa.backend.ai.dto.AiChatResponse;
import com.mhsa.backend.ai.dto.ChatSessionOverviewDto;
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
    private final CrisisDetectionService crisisDetectionService;
    private final PiiScrubberService piiScrubberService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private static final String BASE_SYSTEM_PROMPT = "Ngươi là 'Bạn Tâm Giao', một trợ lý tâm lý học thấu cảm. Luôn xưng là 'mình' và gọi người dùng là 'bạn'. "
            + "Hãy tham khảo [USER CONTEXT] dưới đây để đưa ra lời khuyên cá nhân hóa, nhưng KHÔNG ĐƯỢC nhắc lại dữ liệu một cách máy móc. "
            + "Hãy an ủi tự nhiên. BẮT BUỘC chỉ trả về DUY NHẤT một JSON object hợp lệ theo schema sau, không thêm bất kỳ văn bản nào khác: "
            + "{\"reply\":\"The comforting message to the user\",\"sentiment\":\"NEUTRAL|SAD|ANXIOUS|HAPPY|STRESSED|ANGRY\",\"is_crisis\":true|false}. "
            + "Không được dùng markdown, không dùng ```json, chỉ trả về raw JSON object.";
    private static final String EMERGENCY_RESPONSE = "Mình rất tiếc khi nghe bạn đang ở trạng thái nguy cấp. "
            + "Nếu bạn có ý định làm hại bản thân hoặc người khác, hãy gọi ngay 115 để được hỗ trợ khẩn cấp. "
            + "Bạn cũng có thể gọi 19001567 để được tư vấn và hỗ trợ tâm lý. "
            + "Bạn không phải đối mặt chuyện này một mình.";
    private static final String GEMINI_FALLBACK_RESPONSE = "Mình đang gặp sự cố kết nối tạm thời với AI. Bạn có thể thử lại sau ít phút nhé.";
    private static final String DEFAULT_EMOTION = "NEUTRAL";
    private static final int PREVIEW_MAX_LENGTH = 120;

    /**
     * Send a message to Gemini AI and get a response
     *
     * @param request The user's chat request
     * @return The AI response
     */
    public AiChatResponse sendMessage(AiChatRequest request, UUID profileId) {
        final ChatSession[] sessionRef = new ChatSession[1];
        try {
            log.info("Sending message to Gemini API. SessionId: {}", request.getSessionId());
            String rawUserMessage = request.getContent();

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
            sessionRef[0] = session;

            // 2. Save user's raw message for therapist review/audit.
            ChatMessage userMessage = ChatMessage.builder()
                    .session(session)
                    .sender("USER")
                    .content(rawUserMessage)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
            chatMessageRepository.save(userMessage);

            // 3. Detect crisis intent and short-circuit with emergency response.
            if (crisisDetectionService.isCrisisDetected(rawUserMessage)) {
                ChatMessage emergencyMessage = ChatMessage.builder()
                        .session(session)
                        .sender("AI")
                        .content(EMERGENCY_RESPONSE)
                        .sentAt(java.time.LocalDateTime.now())
                        .build();
                chatMessageRepository.save(emergencyMessage);

                return AiChatResponse.builder()
                        .sessionId(session.getId().toString())
                        .messageId(emergencyMessage.getId().toString())
                        .content(EMERGENCY_RESPONSE)
                        .sentimentDetected("CRISIS")
                        .crisisDetected(true)
                        .build();
            }

            String sanitizedUserMessage = piiScrubberService.scrub(rawUserMessage);

            // 4. Get last messages for context (sanitized before outbound call).
            List<ChatMessage> lastMessages = chatMessageRepository.findTop10BySessionOrderBySentAtDesc(session);
            Collections.reverse(lastMessages); // oldest first
            StringBuilder chatHistoryBuilder = new StringBuilder();
            for (ChatMessage msg : lastMessages) {
                String sanitizedHistoryContent = piiScrubberService.scrub(msg.getContent());
                chatHistoryBuilder.append(msg.getSender()).append(": ").append(sanitizedHistoryContent).append("\n");
            }
            String chatHistory = chatHistoryBuilder.toString();

            // 5. Get and sanitize user context summary before cloud transmission.
            String userContext = contextAggregatorService.getUserContextSummary(profileId);
            String sanitizedUserContext = piiScrubberService.scrub(userContext);

            // 6. Compose the mega system prompt
            String systemPrompt = BASE_SYSTEM_PROMPT + "\n" + sanitizedUserContext;

            // 7. Build prompt: system prompt + sanitized history + sanitized incoming message
            StringBuilder megaPrompt = new StringBuilder();
            megaPrompt.append(systemPrompt).append("\n");
            megaPrompt.append(chatHistory);
            megaPrompt.append("USER: ").append(sanitizedUserMessage);

            // 8. Build Gemini request
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
                ParsedGeminiResult parsedGeminiResult = parseGeminiResponse(response.getBody());
                log.info("Successfully received response from Gemini API");

                ChatMessage aiMessage = ChatMessage.builder()
                        .session(session)
                        .sender("AI")
                        .content(parsedGeminiResult.reply())
                        .sentAt(java.time.LocalDateTime.now())
                        .build();
                chatMessageRepository.save(aiMessage);

                return AiChatResponse.builder()
                        .sessionId(session.getId().toString())
                        .messageId(aiMessage.getId().toString())
                        .content(parsedGeminiResult.reply())
                        .sentimentDetected(parsedGeminiResult.sentiment())
                        .crisisDetected(parsedGeminiResult.isCrisis())
                        .build();
            }

            log.error("Gemini API returned non-success status: {}", response.getStatusCode());
            return buildFallbackResponse(session, "Gemini API returned non-success status");

        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Gemini API key rejected (likely leaked or invalid). Rotate the key and update GEMINI_API_KEY.", e);
            return buildFallbackResponse(sessionRef[0], "Gemini API key rejected");
        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            return buildFallbackResponse(sessionRef[0], "Failed to communicate with Gemini API");
        } catch (Exception e) {
            log.error("Error processing Gemini response", e);
            throw new RuntimeException("Error processing AI response", e);
        }
    }

    public List<ChatSessionOverviewDto> getSessionOverviews(UUID profileId) {
        List<ChatSession> sessions = chatSessionRepository.findAllByProfileIdOrderByUpdatedAtDesc(profileId);

        return sessions.stream()
                .map(session -> {
                    ChatMessage latestMessage = chatMessageRepository
                            .findTopBySessionOrderBySentAtDesc(session)
                            .orElse(null);

                    ChatMessage latestUserMessage = chatMessageRepository
                            .findTopBySessionAndSenderOrderBySentAtDesc(session, "USER")
                            .orElse(null);

                    String preview = latestUserMessage == null
                            ? ""
                            : buildPreview(latestUserMessage.getContent());

                    return ChatSessionOverviewDto.builder()
                            .sessionId(session.getId().toString())
                            .updatedAt(latestMessage == null ? session.getCreatedAt() : latestMessage.getSentAt())
                            .preview(preview)
                            .emotion(DEFAULT_EMOTION)
                            .build();
                })
                .toList();
    }

    private String buildPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PREVIEW_MAX_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, PREVIEW_MAX_LENGTH - 3) + "...";
    }

    /**
     * Build the request payload for Gemini API
     *
     * @param megaPrompt The final prompt sent to Gemini
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
        generationConfig.put("response_mime_type", "application/json");
        request.put("generation_config", generationConfig);

        return request;
    }

    /**
     * Parse the response from Gemini API and extract the text content
     *
     * @param responseBody The JSON response body from Gemini
     * @return The extracted text response
     */
    private ParsedGeminiResult parseGeminiResponse(String responseBody) throws Exception {
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
                    return parseStructuredGeminiText(textNode.asText());
                }
            }
        }

        throw new RuntimeException("Unable to parse Gemini API response");
    }

    private ParsedGeminiResult parseStructuredGeminiText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedGeminiResult(
                    "Mình đang gặp chút trục trặc kỹ thuật. Bạn thử nhắn lại giúp mình nhé.",
                    DEFAULT_EMOTION,
                    false
            );
        }

        try {
            JsonNode parsedNode = objectMapper.readTree(rawText);
            String reply = parsedNode.path("reply").asText("").trim();
            String sentiment = normalizeSentiment(parsedNode.path("sentiment").asText(DEFAULT_EMOTION));
            boolean isCrisis = parsedNode.path("is_crisis").asBoolean(false);

            if (!reply.isBlank()) {
                return new ParsedGeminiResult(reply, sentiment, isCrisis);
            }
        } catch (JsonProcessingException ignored) {
            // Fallback below handles markdown-wrapped JSON or plain text outputs.
        }

        String extractedJson = extractJsonObject(rawText);
        if (extractedJson != null) {
            try {
                JsonNode parsedNode = objectMapper.readTree(extractedJson);
                String reply = parsedNode.path("reply").asText("").trim();
                if (!reply.isBlank()) {
                    String sentiment = normalizeSentiment(parsedNode.path("sentiment").asText(DEFAULT_EMOTION));
                    boolean isCrisis = parsedNode.path("is_crisis").asBoolean(false);
                    return new ParsedGeminiResult(reply, sentiment, isCrisis);
                }
            } catch (JsonProcessingException ignored) {
                // Fall through to plain text fallback.
            }
        }

        String fallbackReply = rawText.replace("```json", "")
                .replace("```", "")
                .trim();
        if (fallbackReply.isBlank()) {
            fallbackReply = "Mình đang gặp chút trục trặc kỹ thuật. Bạn thử nhắn lại giúp mình nhé.";
        }

        return new ParsedGeminiResult(
                fallbackReply,
                DEFAULT_EMOTION,
                crisisDetectionService.isCrisisDetected(fallbackReply)
        );
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private String normalizeSentiment(String sentiment) {
        if (sentiment == null || sentiment.isBlank()) {
            return DEFAULT_EMOTION;
        }
        return sentiment.trim().toUpperCase();
    }

    private AiChatResponse buildFallbackResponse(ChatSession session, String reason) {
        if (session == null) {
            log.warn("Returning fallback AI response without persisted session because: {}", reason);
            return AiChatResponse.builder()
                    .sessionId(null)
                    .messageId(null)
                    .content(GEMINI_FALLBACK_RESPONSE)
                    .sentimentDetected(DEFAULT_EMOTION)
                    .crisisDetected(false)
                    .build();
        }

        log.warn("Returning fallback AI response for session {} because: {}", session.getId(), reason);

        ChatMessage fallbackMessage = ChatMessage.builder()
                .session(session)
                .sender("AI")
                .content(GEMINI_FALLBACK_RESPONSE)
                .sentAt(java.time.LocalDateTime.now())
                .build();
        chatMessageRepository.save(fallbackMessage);

        return AiChatResponse.builder()
                .sessionId(session.getId().toString())
                .messageId(fallbackMessage.getId().toString())
                .content(GEMINI_FALLBACK_RESPONSE)
                .sentimentDetected(DEFAULT_EMOTION)
                .crisisDetected(false)
                .build();
    }

    private record ParsedGeminiResult(String reply, String sentiment, boolean isCrisis) {

    }
}
