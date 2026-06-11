package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.TreasureRequest;
import com.mhsa.backend.tracking.dto.TreasureResponse;
import com.mhsa.backend.tracking.entity.Treasure;

@Component
public class TreasureMapper {

    public Treasure toEntity(TreasureRequest dto) {
        if (dto == null) {
            return null;
        }

        Treasure entity = new Treasure();
        entity.setCategory(dto.getCategory());
        entity.setContent(dto.getContent());
        entity.setEmoji(dto.getEmoji());
        return entity;
    }

    /**
     * Maps a persisted treasure to its response. The media URL is generated fresh by the
     * caller (presigned, short-lived) and passed in; null for text-only treasures.
     */
    public TreasureResponse toResponseDTO(Treasure entity, String mediaUrl) {
        if (entity == null) {
            return null;
        }

        return TreasureResponse.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .content(entity.getContent())
                .emoji(entity.getEmoji())
                .mediaUrl(mediaUrl)
                .mediaType(entity.getMediaType() == null ? null : entity.getMediaType().name())
                .mimeType(entity.getMimeType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
