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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private static final String SYSTEM_PROMPT = "Ngươi là 'Bạn Tâm Giao', một trợ lý tâm lý học thấu cảm dành cho sinh viên Việt Nam. "
            + "Luôn xưng là 'mình' và gọi người dùng là 'bạn'. Câu trả lời phải ngắn gọn, ấm áp, thấu hiểu và mang tính xoa dịu.";

    /**
     * Send a message to Gemini AI and get a response
     *
     * @param request The user's chat request
     * @return The AI response
     */
    public AiChatResponse sendMessage(AiChatRequest request) {
        try {
            log.info("Sending message to Gemini API. SessionId: {}", request.getSessionId());

            // Generate sessionId if not provided
            String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                    ? request.getSessionId()
                    : UUID.randomUUID().toString();

            // Build the request payload for Gemini API
            Map<String, Object> requestPayload = buildGeminiRequest(request.getContent());

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

                return AiChatResponse.builder()
                        .sessionId(sessionId)
                        .messageId(UUID.randomUUID().toString())
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
    private Map<String, Object> buildGeminiRequest(String userMessage) {
        Map<String, Object> request = new HashMap<>();

        // Add system instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, String> parts = new HashMap<>();
        parts.put("text", SYSTEM_PROMPT);
        systemInstruction.put("parts", parts);
        request.put("system_instruction", systemInstruction);

        // Add user message as contents
        List<Map<String, Object>> contents = List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("text", userMessage)
                        )
                )
        );
        request.put("contents", contents);

        // Add safety settings for mental health context
        // BLOCK_ONLY_HIGH ensures the AI doesn't refuse to engage with users expressing mental health concerns
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
        generationConfig.put("max_output_tokens", 1024);
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
