package com.mhsa.backend.tracking.dto;

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

    @Schema(description = "Optional diary title", example = "A Productive Monday")
    private String title;

    @NotBlank
    @Schema(description = "Plain-text diary content entered by user", example = "Today was a productive day, I felt really focused.")
    private String content;

    @Schema(description = "Mood tag for this diary entry", example = "MOTIVATED")
    private String moodTag;

    @Min(1)
    @Max(10)
    @Schema(description = "Daily positivity score from 1 to 10", example = "8")
    private Integer positivityScore;
}
