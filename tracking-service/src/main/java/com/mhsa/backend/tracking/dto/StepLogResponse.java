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
public class StepLogResponse {

    @Schema(description = "Step log identifier", example = "e0a4a3fb-5f2f-4061-8f23-cf2cc1773ff0")
    private UUID id;

    @Schema(description = "Number of steps taken", example = "5230")
    private Integer stepCount;

    @Schema(description = "Daily step goal", example = "6000")
    private Integer goal;

    @Schema(description = "Source of the step data", example = "DEVICE_SENSOR")
    private String source;

    @Schema(description = "Step log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;

    @Schema(description = "Time the steps were logged in ISO-8601", example = "2026-04-10T18:30:00")
    private LocalDateTime loggedAt;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-04-10T18:31:10")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-04-10T18:40:00")
    private LocalDateTime updatedAt;
}
