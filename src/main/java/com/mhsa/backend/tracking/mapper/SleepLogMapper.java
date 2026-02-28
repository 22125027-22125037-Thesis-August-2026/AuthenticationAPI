package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.SleepLogRequest;
import com.mhsa.backend.tracking.dto.SleepLogResponse;
import com.mhsa.backend.tracking.entity.SleepLog;

@Component
public class SleepLogMapper {

    public SleepLog toEntity(SleepLogRequest dto) {
        if (dto == null) {
            return null;
        }

        SleepLog entity = new SleepLog();
        entity.setProfileId(dto.getProfileId());
        entity.setSleepStartAt(dto.getBedTime());
        entity.setSleepEndAt(dto.getWakeTime());
        entity.setSleepQuality(dto.getSleepQuality());
        entity.setNote(dto.getNote());

        return entity;
    }

    public SleepLogResponse toResponseDTO(SleepLog entity) {
        if (entity == null) {
            return null;
        }

        return SleepLogResponse.builder()
                .id(entity.getId())
                .profileId(entity.getProfileId())
                .bedTime(entity.getSleepStartAt())
                .wakeTime(entity.getSleepEndAt())
                .durationMinutes(entity.getDurationMinutes())
                .sleepQuality(entity.getSleepQuality())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
