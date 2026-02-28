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
public class MoodLogResponse {

    @Schema(description = "Mood log identifier", example = "8b2af1c7-fd57-4695-ac34-7c915600fd2f")
    private UUID id;

    @Schema(description = "Profile identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID profileId;

    @Schema(description = "Mood positivity score from 1 to 10", example = "8")
    private Integer positivityScore;

    @Schema(description = "Optional note for mood context", example = "Felt calm after a short walk.")
    private String note;

    @Schema(description = "Log timestamp in ISO-8601", example = "2026-02-28T20:15:00")
    private LocalDateTime logDate;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-02-28T20:15:05")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-02-28T20:30:00")
    private LocalDateTime updatedAt;
}
