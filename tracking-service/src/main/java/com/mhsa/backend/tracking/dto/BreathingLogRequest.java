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
public class BreathingLogRequest {

    @PositiveOrZero
    @Schema(description = "Duration of this breathing session in seconds", example = "120")
    private int durationSeconds;

    @Schema(description = "Daily breathing goal in seconds", example = "300")
    private Integer goalSeconds;

    @Schema(description = "Source of the breathing data", example = "GUIDED_SESSION")
    private String source;

    @Schema(description = "Breathing log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;
}
