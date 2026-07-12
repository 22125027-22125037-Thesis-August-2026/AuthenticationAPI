package com.mhsa.backend.tracking.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepLogRequest {

    @PositiveOrZero
    @Schema(description = "Number of steps taken", example = "5230")
    private int stepCount;

    @Schema(description = "Daily step goal", example = "6000")
    private Integer goal;

    @Schema(description = "Source of the step data", example = "DEVICE_SENSOR")
    private String source;

    @Schema(description = "Step log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;
}
