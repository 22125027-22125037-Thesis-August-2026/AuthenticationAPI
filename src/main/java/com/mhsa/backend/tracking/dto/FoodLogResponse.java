package com.mhsa.backend.tracking.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodLogResponse {

    @Schema(description = "Food log identifier", example = "35f72f8d-a640-4f39-a8f0-2064f723bd6a")
    private UUID id;

    @Schema(description = "Meal type", example = "LUNCH")
    private String mealType;

    @Schema(description = "Food description", example = "Grilled chicken, brown rice, and mixed vegetables")
    private String foodDescription;

    @Schema(description = "Satiety level after meal", example = "COMFORTABLY_FULL")
    private String satietyLevel;

    @Schema(description = "Food log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-03-04T12:30:00")
    private LocalDateTime createdAt;
}
