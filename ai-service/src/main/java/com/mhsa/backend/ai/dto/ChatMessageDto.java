package com.mhsa.backend.ai.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    @Schema(description = "Unique identifier for the message", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID messageId;

    @Schema(description = "Unique identifier for the chat session", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID sessionId;

    @Schema(description = "Message sender", example = "USER")
    private String sender;

    @Schema(description = "Message content", example = "Mình thấy rất lo lắng gần đây.")
    private String content;

    @Schema(description = "Timestamp when the message was sent", example = "2026-04-05T10:15:30")
    private LocalDateTime sentAt;
}
