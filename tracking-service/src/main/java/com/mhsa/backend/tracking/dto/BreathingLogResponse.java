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
public class BreathingLogResponse {

    @Schema(description = "Breathing log identifier", example = "e0a4a3fb-5f2f-4061-8f23-cf2cc1773ff0")
    private UUID id;

    @Schema(description = "Total breathing duration for the day in seconds", example = "300")
    private Integer totalDurationSeconds;

    @Schema(description = "Number of breathing sessions completed for the day", example = "3")
    private Integer sessionsCompleted;

    @Schema(description = "Daily breathing goal in seconds", example = "300")
    private Integer goalSeconds;

    @Schema(description = "Source of the breathing data", example = "GUIDED_SESSION")
    private String source;

    @Schema(description = "Breathing log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;

    @Schema(description = "Time the breathing was logged in ISO-8601", example = "2026-04-10T18:30:00")
    private LocalDateTime loggedAt;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-04-10T18:31:10")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-04-10T18:40:00")
    private LocalDateTime updatedAt;
}
