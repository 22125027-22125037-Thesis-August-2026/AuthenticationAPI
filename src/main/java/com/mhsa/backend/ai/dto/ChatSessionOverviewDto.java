package com.mhsa.backend.ai.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionOverviewDto {

    @Schema(description = "Chat session id", example = "123e4567-e89b-12d3-a456-426614174000")
    private String sessionId;

    @Schema(description = "Last activity timestamp", example = "2026-04-05T10:15:30")
    private LocalDateTime updatedAt;

    @Schema(description = "Preview text from latest user message", example = "Mình thấy lo lắng gần đây...")
    private String preview;

    @Schema(description = "Dominant emotion for session", example = "NEUTRAL")
    private String emotion;
}
