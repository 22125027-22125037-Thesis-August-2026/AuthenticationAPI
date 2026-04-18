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
public class SleepLogResponse {

    @Schema(description = "Sleep log identifier", example = "e0a4a3fb-5f2f-4061-8f23-cf2cc1773ff0")
    private UUID id;

    @Schema(description = "Bed time in ISO-8601", example = "2026-02-28T22:30:00")
    private LocalDateTime bedTime;

    @Schema(description = "Wake time in ISO-8601", example = "2026-03-01T06:30:00")
    private LocalDateTime wakeTime;

    @Schema(description = "Total sleep duration in minutes", example = "480")
    private Integer durationMinutes;

    @Schema(description = "Sleep quality score from 1 to 10", example = "7")
    private Integer sleepQuality;

    @Schema(description = "Optional sleep note", example = "Slept well with one brief wake-up.")
    private String note;

    @Schema(description = "Sleep log date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-03-01T06:31:10")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-03-01T06:40:00")
    private LocalDateTime updatedAt;
}
