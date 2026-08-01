package com.mhsa.backend.tracking.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.entity.MediaAttachment;

@Component
public class MediaAttachmentMapper {

    public MediaAttachment toEntity(MediaAttachmentRequest dto) {
        if (dto == null) {
            return null;
        }

        MediaAttachment entity = new MediaAttachment();
        entity.setFileUrl(dto.getFileUrl());
        entity.setMimeType(dto.getMimeType());
        entity.setFileSizeBytes(dto.getFileSizeBytes());

        if (dto.getMediaType() != null && !dto.getMediaType().isBlank()) {
            entity.setMediaType(MediaAttachment.MediaType.valueOf(dto.getMediaType().toUpperCase(Locale.ROOT)));
        } else {
            entity.setMediaType(MediaAttachment.MediaType.OTHER);
        }

        if (dto.getReferenceType() != null && "FOOD_LOG".equalsIgnoreCase(dto.getReferenceType())) {
            entity.setFoodLogId(dto.getReferenceId());
        }

        return entity;
    }

    public MediaAttachmentResponse toResponseDTO(MediaAttachment entity) {
        return entity == null ? null : toResponseDTO(entity, entity.getFileUrl());
    }

    /**
     * Same as {@link #toResponseDTO(MediaAttachment)} but with the fileUrl overridden — used by
     * diary attachments, whose "file_url" column actually stores an S3 object key, not a
     * client-facing URL. The caller resolves it to a short-lived presigned GET URL first.
     */
    public MediaAttachmentResponse toResponseDTO(MediaAttachment entity, String fileUrl) {
        if (entity == null) {
            return null;
        }

        return MediaAttachmentResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getMimeType())
                .fileUrl(fileUrl)
                .build();
    }
}
