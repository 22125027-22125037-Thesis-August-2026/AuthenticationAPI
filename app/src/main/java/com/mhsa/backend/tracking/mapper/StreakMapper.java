package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;
import com.mhsa.backend.tracking.entity.Streak;

@Component
public class StreakMapper {

    public Streak toEntity(StreakRequest dto) {
        if (dto == null) {
            return null;
        }

        Streak entity = new Streak();
        entity.setStreakType(dto.getStreakType());
        entity.setCurrentCount(dto.getCurrentCount());
        entity.setLongestCount(dto.getLongestCount());
        entity.setLastLoggedAt(dto.getLastLoggedAt());

        return entity;
    }

    public StreakResponse toResponseDTO(Streak entity) {
        if (entity == null) {
            return null;
        }

        return StreakResponse.builder()
                .id(entity.getId())
                .streakType(entity.getStreakType())
                .currentCount(entity.getCurrentCount())
                .longestCount(entity.getLongestCount())
                .lastLoggedAt(entity.getLastLoggedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
