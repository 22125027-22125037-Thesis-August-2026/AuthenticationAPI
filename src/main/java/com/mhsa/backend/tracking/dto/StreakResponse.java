package com.mhsa.backend.tracking.dto;

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
public class StreakResponse {

    @Schema(description = "Streak identifier", example = "4f927df8-d57f-4475-bf5e-cf8445b1c7f4")
    private UUID id;

    @Schema(description = "Profile identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID profileId;

    @Schema(description = "Streak type", example = "DAILY_TRACKING")
    private String streakType;

    @Schema(description = "Current streak count", example = "5")
    private Integer currentCount;

    @Schema(description = "Longest streak count", example = "12")
    private Integer longestCount;

    @Schema(description = "Last log timestamp in ISO-8601", example = "2026-02-28T21:30:00")
    private LocalDateTime lastLoggedAt;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-02-20T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-02-28T21:30:00")
    private LocalDateTime updatedAt;
}
