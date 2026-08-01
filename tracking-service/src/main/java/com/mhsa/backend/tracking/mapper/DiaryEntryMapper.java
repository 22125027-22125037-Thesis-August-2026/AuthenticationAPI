package com.mhsa.backend.tracking.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.service.TreasureStorageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryEntryMapper {

    // Presigned GET URLs are short-lived; the mobile client re-fetches the list to refresh them.
    private static final long PRESIGNED_URL_VALIDITY_SECONDS = 24 * 3600; // 24 hours

    private final MediaAttachmentMapper mediaAttachmentMapper;
    // Shared object-storage service (see its class comment) — resolves each attachment's
    // stored S3 object key into a short-lived presigned GET URL for the client.
    private final TreasureStorageService mediaStorageService;

    public DiaryEntry toEntity(DiaryEntryRequest dto) {
        if (dto == null) {
            return null;
        }

        DiaryEntry entity = new DiaryEntry();
        entity.setTitle(dto.getTitle());
        entity.setMoodTag(dto.getMoodTag());
        entity.setPositivityScore(dto.getPositivityScore());
        entity.setEntryDate(dto.getEntryDate());

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
                : entity.getMediaAttachments().stream()
                        .map(attachment -> mediaAttachmentMapper.toResponseDTO(attachment, resolvePresignedUrl(attachment)))
                        .toList();

        DiaryEntryResponse.DiaryEntryResponseBuilder builder = DiaryEntryResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .moodTag(entity.getMoodTag())
                .positivityScore(entity.getPositivityScore())
                .entryDate(entity.getEntryDate())
                .attachments(mediaAttachmentResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // TODO: Call EncryptionUtils to decrypt Entity 'encryptedContent' into DTO 'content'
        builder.content(null);

        return builder.build();
    }

    /**
     * Resolves a stored attachment's object key into a client-facing presigned GET URL.
     * Rows created before real S3 storage was wired up hold a fake "/files/..." path that was
     * never actually written to a bucket — those resolve to null so the client shows a
     * "couldn't load" placeholder instead of a broken image request.
     */
    private String resolvePresignedUrl(MediaAttachment attachment) {
        String objectKey = attachment.getFileUrl();
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/files/")) {
            return null;
        }
        return mediaStorageService.generatePresignedUrl(objectKey, PRESIGNED_URL_VALIDITY_SECONDS);
    }
}
