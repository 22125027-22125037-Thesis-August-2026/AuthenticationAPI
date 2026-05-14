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
        if (entity == null) {
            return null;
        }

        return MediaAttachmentResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getMimeType())
                .fileUrl(entity.getFileUrl())
                .build();
    }
}
