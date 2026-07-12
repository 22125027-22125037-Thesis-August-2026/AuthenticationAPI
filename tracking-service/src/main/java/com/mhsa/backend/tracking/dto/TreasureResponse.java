package com.mhsa.backend.tracking.dto;

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
public class TreasureResponse {

    @Schema(description = "Treasure identifier", example = "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f")
    private UUID id;

    @Schema(description = "Treasure category id", example = "people")
    private String category;

    @Schema(description = "Short note the user wants to remember", example = "Mẹ luôn để dành phần cơm và đợi mình về.")
    private String content;

    @Schema(description = "Emoji that anchors this treasure", example = "❤️")
    private String emoji;

    @Schema(description = "Freshly signed media URL, null when the treasure is text-only",
            example = "http://localhost:9000/mhsa-media/treasures/...")
    private String mediaUrl;

    @Schema(description = "Media kind: IMAGE, AUDIO or VIDEO; null when text-only", example = "IMAGE")
    private String mediaType;

    @Schema(description = "Media MIME type; null when text-only", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-06-11T21:12:20")
    private LocalDateTime createdAt;
}
