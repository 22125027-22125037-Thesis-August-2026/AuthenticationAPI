package com.mhsa.backend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    @Schema(
            description = "Unique identifier for the chat session",
            example = "session-12345"
    )
    private String sessionId;

    @Schema(
            description = "Unique identifier for the message",
            example = "msg-67890"
    )
    private String messageId;

    @Schema(
            description = "Content of the AI's response",
            example = "Hello, how can I help you today?"
    )
    private String content;

    @Schema(
            description = "Detected sentiment in the message",
            example = "positive"
    )
    private String sentimentDetected;

    @Schema(
            description = "Indicates if a crisis was detected in the message",
            example = "false"
    )
    private boolean crisisDetected;
}
