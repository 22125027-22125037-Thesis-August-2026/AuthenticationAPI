package com.mhsa.backend.tracking.mapper;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.StepLogRequest;
import com.mhsa.backend.tracking.dto.StepLogResponse;
import com.mhsa.backend.tracking.entity.StepLog;

@Component
public class StepLogMapper {

    public StepLog toEntity(StepLogRequest dto) {
        if (dto == null) {
            return null;
        }

        StepLog entity = new StepLog();
        entity.setStepCount(dto.getStepCount());
        entity.setGoal(dto.getGoal());
        entity.setSource(dto.getSource());
        entity.setEntryDate(dto.getEntryDate());

        return entity;
    }

    public StepLogResponse toResponseDTO(StepLog entity) {
        if (entity == null) {
            return null;
        }

        return StepLogResponse.builder()
                .id(entity.getId())
                .stepCount(entity.getStepCount())
                .goal(entity.getGoal())
                .source(entity.getSource())
                .entryDate(entity.getEntryDate())
                .loggedAt(entity.getLoggedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
