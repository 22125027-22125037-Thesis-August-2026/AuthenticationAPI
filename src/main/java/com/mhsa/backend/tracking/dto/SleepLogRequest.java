package com.mhsa.backend.tracking.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepLogRequest {

    @NotNull
    @Schema(description = "Bed time in ISO-8601", example = "2026-02-28T22:30:00")
    private LocalDateTime bedTime;

    @NotNull
    @Schema(description = "Wake time in ISO-8601", example = "2026-03-01T06:30:00")
    private LocalDateTime wakeTime;

    @Schema(description = "Sleep quality score from 1 to 10", example = "7")
    private Integer sleepQuality;

    @Schema(description = "Optional sleep note", example = "Slept well with one brief wake-up.")
    private String note;
}
