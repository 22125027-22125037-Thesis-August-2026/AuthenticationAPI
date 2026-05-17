package com.mhsa.backend.contract;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingContextResponse {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SleepContext {
        private double avgHours;
        private int poorDays;
        private int totalDays;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MoodContext {
        private String dominantEmotion;
        private double avgScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiaryContext {
        private List<String> dominantEmotions;
        private int totalEntries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodContext {
        private int lowWaterDays;
        private int skippedMeals;
        private int totalDays;
    }

    private SleepContext sleep;
    private MoodContext mood;
    private DiaryContext diary;
    private FoodContext food;
}
