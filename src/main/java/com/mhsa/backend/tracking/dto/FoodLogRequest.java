package com.mhsa.backend.tracking.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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

    @Min(0)
    @Schema(description = "Water glasses logged for the day", example = "6")
    private Integer waterGlasses;

    @NotBlank
    @Schema(description = "Food description", example = "Grilled chicken, brown rice, and mixed vegetables")
    private String foodDescription;

    @NotBlank
    @Schema(description = "Satiety level after meal", example = "COMFORTABLY_FULL")
    private String satietyLevel;

    @Schema(description = "Food log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;
}
