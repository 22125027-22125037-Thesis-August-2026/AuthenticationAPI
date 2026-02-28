package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;
import com.mhsa.backend.tracking.entity.MoodLog;

@Component
public class MoodLogMapper {

    public MoodLog toEntity(MoodLogRequest dto) {
        if (dto == null) {
            return null;
        }

        MoodLog entity = new MoodLog();
        entity.setProfileId(dto.getProfileId());
        entity.setMoodScore(dto.getPositivityScore());
        entity.setNote(dto.getNote());
        if (dto.getLogDate() != null) {
            entity.setLoggedAt(dto.getLogDate());
        }

        return entity;
    }

    public MoodLogResponse toResponseDTO(MoodLog entity) {
        if (entity == null) {
            return null;
        }

        return MoodLogResponse.builder()
                .id(entity.getId())
                .profileId(entity.getProfileId())
                .positivityScore(entity.getMoodScore())
                .note(entity.getNote())
                .logDate(entity.getLoggedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
