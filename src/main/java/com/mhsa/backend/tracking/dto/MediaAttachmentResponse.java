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
public class MediaAttachmentResponse {

    @Schema(description = "Media attachment identifier", example = "d1d3093b-c3df-4246-8301-b2ac6f6af9f3")
    private UUID id;

    @Schema(description = "Profile identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID profileId;

    @Schema(description = "Target reference identifier", example = "4c8de90a-7f59-4906-8619-8416fd8c57d3")
    private UUID referenceId;

    @Schema(description = "Reference type", example = "DIARY_ENTRY")
    private String referenceType;

    @Schema(description = "Public or signed media URL", example = "https://cdn.mhsa.app/media/diary/entry-01.jpg")
    private String fileUrl;

    @Schema(description = "Media type enum value", example = "IMAGE")
    private String mediaType;

    @Schema(description = "MIME type", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "245760")
    private Long fileSizeBytes;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-02-28T21:22:10")
    private LocalDateTime createdAt;
}
