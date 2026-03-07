package com.mhsa.backend.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodLogRequest {

    @NotBlank
    @Schema(description = "Meal type", example = "LUNCH")
    private String mealType;

    @NotBlank
    @Schema(description = "Food description", example = "Grilled chicken, brown rice, and mixed vegetables")
    private String foodDescription;

    @NotBlank
    @Schema(description = "Satiety level after meal", example = "COMFORTABLY_FULL")
    private String satietyLevel;
}
