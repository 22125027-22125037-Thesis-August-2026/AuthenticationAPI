package com.mhsa.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("content")
    @NotBlank(message = "Message content cannot be empty")
    private String content;
}
