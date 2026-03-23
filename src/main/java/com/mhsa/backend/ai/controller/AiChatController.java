package com.mhsa.backend.ai.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.ai.dto.AiChatRequest;
import com.mhsa.backend.ai.dto.AiChatResponse;
import com.mhsa.backend.ai.service.GeminiAiService;
import com.mhsa.backend.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
@Slf4j
public class AiChatController {

    private final GeminiAiService geminiAiService;

    /**
     * Send a message to the AI chatbot and receive a response
     *
     * @param profileId The user's profile ID from the X-Profile-Id header
     * @param request The chat request containing user message and optional
     * session ID
     * @return The API response wrapped with metadata
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @RequestHeader("X-Profile-Id") UUID profileId,
            @Valid @RequestBody AiChatRequest request) {

        log.info("Received chat request from profileId: {} with sessionId: {}", profileId, request.getSessionId());

        try {
            AiChatResponse response = geminiAiService.sendMessage(request);
            log.info("Chat request processed successfully. MessageId: {}", response.getMessageId());

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error processing chat request for profileId: {}", profileId, e);

            ApiResponse<AiChatResponse> errorResponse = ApiResponse.error("An error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
