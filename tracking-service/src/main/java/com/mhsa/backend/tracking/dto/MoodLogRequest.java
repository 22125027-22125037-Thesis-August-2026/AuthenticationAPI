package com.mhsa.backend.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodLogRequest {

    @Min(1)
    @Max(10)
    @Schema(description = "Mood positivity score from 1 to 10", example = "8")
    private Integer positivityScore;

    @Schema(description = "Optional note for mood context", example = "Felt calm after a short walk.")
    private String note;
}
