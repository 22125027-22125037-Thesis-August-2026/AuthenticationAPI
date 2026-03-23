package com.mhsa.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("sentiment_detected")
    private String sentimentDetected;

    @JsonProperty("crisis_detected")
    private boolean crisisDetected;
}
