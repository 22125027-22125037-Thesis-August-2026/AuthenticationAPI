package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.BreathingLogRequest;
import com.mhsa.backend.tracking.dto.BreathingLogResponse;
import com.mhsa.backend.tracking.entity.BreathingLog;

@Component
public class BreathingLogMapper {

    public BreathingLog toEntity(BreathingLogRequest dto) {
        if (dto == null) {
            return null;
        }

        BreathingLog entity = new BreathingLog();
        entity.setTotalDurationSeconds(dto.getDurationSeconds());
        entity.setGoalSeconds(dto.getGoalSeconds());
        entity.setSource(dto.getSource());
        entity.setEntryDate(dto.getEntryDate());

        return entity;
    }

    public BreathingLogResponse toResponseDTO(BreathingLog entity) {
        if (entity == null) {
            return null;
        }

        return BreathingLogResponse.builder()
                .id(entity.getId())
                .totalDurationSeconds(entity.getTotalDurationSeconds())
                .sessionsCompleted(entity.getSessionsCompleted())
                .goalSeconds(entity.getGoalSeconds())
                .source(entity.getSource())
                .entryDate(entity.getEntryDate())
                .loggedAt(entity.getLoggedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
