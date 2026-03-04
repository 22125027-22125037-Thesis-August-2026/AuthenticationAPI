package com.mhsa.backend.tracking.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryEntryMapper {

    private final MediaAttachmentMapper mediaAttachmentMapper;

    public DiaryEntry toEntity(DiaryEntryRequest dto) {
        if (dto == null) {
            return null;
        }

        DiaryEntry entity = new DiaryEntry();
        entity.setMoodTag(dto.getMoodTag());
        entity.setPositivityScore(dto.getPositivityScore());

        // TODO: Call EncryptionUtils to encrypt DTO 'content' into Entity 'encryptedContent'
        entity.setContent(null);

        return entity;
    }

    public DiaryEntryResponse toResponseDTO(DiaryEntry entity) {
        if (entity == null) {
            return null;
        }

        List<MediaAttachmentResponse> mediaAttachmentResponses = entity.getMediaAttachments() == null
                ? Collections.emptyList()
                : entity.getMediaAttachments().stream().map(mediaAttachmentMapper::toResponseDTO).toList();

        DiaryEntryResponse.DiaryEntryResponseBuilder builder = DiaryEntryResponse.builder()
                .id(entity.getId())
                .moodTag(entity.getMoodTag())
                .positivityScore(entity.getPositivityScore())
                .attachments(mediaAttachmentResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // TODO: Call EncryptionUtils to decrypt Entity 'encryptedContent' into DTO 'content'
        builder.content(null);

        return builder.build();
    }
}
