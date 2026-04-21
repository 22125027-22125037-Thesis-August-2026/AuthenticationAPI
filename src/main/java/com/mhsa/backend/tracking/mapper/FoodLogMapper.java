package com.mhsa.backend.tracking.mapper;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.FoodLogRequest;
import com.mhsa.backend.tracking.dto.FoodLogResponse;
import com.mhsa.backend.tracking.entity.FoodLog;

@Component
public class FoodLogMapper {

    public FoodLog toEntity(FoodLogRequest dto) {
        if (dto == null) {
            return null;
        }

        FoodLog entity = new FoodLog();
        entity.setWaterGlasses(Objects.requireNonNullElse(dto.getWaterGlasses(), 0));
        entity.setFoodDescription(dto.getFoodDescription());
        entity.setSatietyLevel(dto.getSatietyLevel());
        entity.setEntryDate(dto.getEntryDate());
        return entity;
    }

    public FoodLogResponse toResponseDTO(FoodLog entity) {
        if (entity == null) {
            return null;
        }

        return FoodLogResponse.builder()
                .id(entity.getId())
                .waterGlasses(entity.getWaterGlasses())
                .foodDescription(entity.getFoodDescription())
                .satietyLevel(entity.getSatietyLevel())
                .entryDate(entity.getEntryDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
