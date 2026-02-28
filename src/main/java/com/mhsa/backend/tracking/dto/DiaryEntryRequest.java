package com.mhsa.backend.tracking.dto;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
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
public class DiaryEntryRequest {

    @Schema(description = "Profile identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID profileId;

    @Schema(description = "Diary title", example = "Focused and productive day")
    private String title;

    @NotBlank
    @Schema(description = "Plain-text diary content entered by user", example = "Today was a productive day, I felt really focused.")
    private String content;

    @Min(1)
    @Max(10)
    @Schema(description = "Daily positivity score from 1 to 10", example = "8")
    private Integer positivityScore;

    @Schema(description = "Entry date in ISO-8601 date format", example = "2026-02-28")
    private LocalDate entryDate;
}
