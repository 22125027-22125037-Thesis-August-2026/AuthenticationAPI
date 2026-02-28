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
        entity.setProfileId(dto.getProfileId());
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

        MediaAttachmentResponse.MediaAttachmentResponseBuilder builder = MediaAttachmentResponse.builder()
                .id(entity.getId())
                .profileId(entity.getProfileId())
                .fileUrl(entity.getFileUrl())
                .mediaType(entity.getMediaType() != null ? entity.getMediaType().name() : null)
                .mimeType(entity.getMimeType())
                .fileSizeBytes(entity.getFileSizeBytes())
                .createdAt(entity.getCreatedAt());

        if (entity.getDiaryEntry() != null) {
            builder.referenceType("DIARY_ENTRY");
            builder.referenceId(entity.getDiaryEntry().getId());
        } else if (entity.getFoodLogId() != null) {
            builder.referenceType("FOOD_LOG");
            builder.referenceId(entity.getFoodLogId());
        }

        return builder.build();
    }
}
