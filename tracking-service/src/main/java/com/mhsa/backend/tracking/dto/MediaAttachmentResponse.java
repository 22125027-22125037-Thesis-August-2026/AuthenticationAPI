package com.mhsa.backend.tracking.dto;

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

    @Schema(description = "Original uploaded file name", example = "entry-photo.jpg")
    private String fileName;

    @Schema(description = "Uploaded file MIME type", example = "image/jpeg")
    private String fileType;

    @Schema(description = "Public or signed media URL", example = "https://cdn.mhsa.app/media/diary/entry-01.jpg")
    private String fileUrl;
}
