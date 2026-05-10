package com.mhsa.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    @Schema(description = "Current emotion score", example = "80")
    private Integer emotionScore;

    @Schema(description = "Dominant mood label", example = "SAD")
    private String dominantMood;

    @Schema(description = "Sleep quality label", example = "Rất tốt")
    private String sleepQuality;

    @Schema(description = "Sleep score in x/9 format", example = "7/9")
    private String sleepScore;

    @Schema(description = "Current diary streak", example = "64")
    private Integer diaryStreak;

    @Schema(description = "Current food status", example = "Lành mạnh")
    private String foodStatus;

    @Schema(description = "Total AI sessions of this profile", example = "25")
    private Integer totalAiSessions;

    @Schema(description = "AI sessions in current month", example = "6")
    private Integer monthlyAiSessions;
}
