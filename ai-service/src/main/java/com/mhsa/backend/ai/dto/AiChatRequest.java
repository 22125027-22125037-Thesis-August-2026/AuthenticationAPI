package com.mhsa.backend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @Schema(
            description = "Unique identifier for the chat session",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private String sessionId;

    @Schema(
            description = "Content of the message to send",
            example = "Hello, how can I help you?"
    )
    @NotBlank(message = "Message content cannot be empty")
    private String content;
}
