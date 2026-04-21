package com.mhsa.backend.dashboard.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mhsa.backend.ai.repository.ChatSessionRepository;
import com.mhsa.backend.dashboard.dto.DashboardSummaryDto;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.FoodLog;
import com.mhsa.backend.tracking.entity.MoodLog;
import com.mhsa.backend.tracking.entity.SleepLog;
import com.mhsa.backend.tracking.entity.Streak;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.FoodLogRepository;
import com.mhsa.backend.tracking.repository.MoodLogRepository;
import com.mhsa.backend.tracking.repository.SleepLogRepository;
import com.mhsa.backend.tracking.repository.StreakRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String DEFAULT_DOMINANT_MOOD = "NEUTRAL";
    private static final String DEFAULT_SLEEP_QUALITY = "Chưa có dữ liệu";
    private static final String DEFAULT_SLEEP_SCORE = "7/9";
    private static final String DEFAULT_FOOD_STATUS = "Lành mạnh";
    private static final int DEFAULT_EMOTION_SCORE = 80;

    private final ChatSessionRepository chatSessionRepository;
    private final MoodLogRepository moodLogRepository;
    private final SleepLogRepository sleepLogRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final FoodLogRepository foodLogRepository;
    private final StreakRepository streakRepository;

    public DashboardSummaryDto getSummary(UUID profileId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        long totalAiSessions = chatSessionRepository.countByProfileId(profileId);
        long monthlyAiSessions = chatSessionRepository.countByProfileIdAndCreatedAtBetween(profileId, monthStart, now);

        MoodLog latestMoodLog = moodLogRepository.findTopByProfileIdOrderByLoggedAtDesc(profileId).orElse(null);
        SleepLog latestSleepLog = sleepLogRepository.findTopByProfileIdOrderByCreatedAtDesc(profileId).orElse(null);
        DiaryEntry latestDiaryEntry = diaryEntryRepository.findTopByProfileIdOrderByCreatedAtDesc(profileId).orElse(null);
        FoodLog latestFoodLog = foodLogRepository.findTopByProfileIdOrderByEntryDateDesc(profileId).orElse(null);
        Streak diaryStreak = streakRepository.findTopByProfileIdAndStreakTypeIgnoreCase(profileId, "DIARY").orElse(null);

        Integer emotionScore = resolveEmotionScore(latestMoodLog, latestDiaryEntry);
        String dominantMood = resolveDominantMood(latestMoodLog, latestDiaryEntry);
        String sleepQuality = resolveSleepQuality(latestSleepLog);
        String sleepScore = resolveSleepScore(latestSleepLog);
        Integer diaryStreakCount = 0;
        if (diaryStreak != null && diaryStreak.getCurrentCount() != null) {
            diaryStreakCount = diaryStreak.getCurrentCount();
        }
        String foodStatus = resolveFoodStatus(latestFoodLog);

        return DashboardSummaryDto.builder()
                .emotionScore(emotionScore)
                .dominantMood(dominantMood)
                .sleepQuality(sleepQuality)
                .sleepScore(sleepScore)
                .diaryStreak(diaryStreakCount)
                .foodStatus(foodStatus)
                .totalAiSessions(Math.toIntExact(totalAiSessions))
                .monthlyAiSessions(Math.toIntExact(monthlyAiSessions))
                .build();
    }

    private Integer resolveEmotionScore(MoodLog latestMoodLog, DiaryEntry latestDiaryEntry) {
        if (latestMoodLog != null && latestMoodLog.getMoodScore() != null) {
            return latestMoodLog.getMoodScore();
        }
        if (latestDiaryEntry != null && latestDiaryEntry.getPositivityScore() != null) {
            return latestDiaryEntry.getPositivityScore();
        }
        return DEFAULT_EMOTION_SCORE;
    }

    private String resolveDominantMood(MoodLog latestMoodLog, DiaryEntry latestDiaryEntry) {
        if (latestDiaryEntry != null && latestDiaryEntry.getMoodTag() != null && !latestDiaryEntry.getMoodTag().isBlank()) {
            return latestDiaryEntry.getMoodTag().trim().toUpperCase(Locale.ROOT);
        }
        if (latestMoodLog != null && latestMoodLog.getMoodScore() != null) {
            int score = latestMoodLog.getMoodScore();
            if (score <= 30) {
                return "SAD";
            }
            if (score <= 45) {
                return "STRESSED";
            }
            if (score <= 60) {
                return "NEUTRAL";
            }
            if (score <= 75) {
                return "CALM";
            }
            return "HAPPY";
        }
        return DEFAULT_DOMINANT_MOOD;
    }

    private String resolveSleepQuality(SleepLog latestSleepLog) {
        if (latestSleepLog == null || latestSleepLog.getSleepQuality() == null) {
            return DEFAULT_SLEEP_QUALITY;
        }

        int score = latestSleepLog.getSleepQuality();
        if (score <= 3) {
            return "Thiếu ngủ";
        }
        if (score <= 6) {
            return "Tạm ổn";
        }
        return "Rất tốt";
    }

    private String resolveSleepScore(SleepLog latestSleepLog) {
        if (latestSleepLog == null || latestSleepLog.getSleepQuality() == null) {
            return DEFAULT_SLEEP_SCORE;
        }
        return latestSleepLog.getSleepQuality() + "/9";
    }

    private String resolveFoodStatus(FoodLog latestFoodLog) {
        if (latestFoodLog == null) {
            return DEFAULT_FOOD_STATUS;
        }

        String foodDescription = latestFoodLog.getFoodDescription() == null ? "" : latestFoodLog.getFoodDescription().toLowerCase(Locale.ROOT);
        String satietyLevel = latestFoodLog.getSatietyLevel() == null ? "" : latestFoodLog.getSatietyLevel().toLowerCase(Locale.ROOT);

        if ((latestFoodLog.getWaterGlasses() != null && latestFoodLog.getWaterGlasses() >= 8)
                || satietyLevel.contains("full")
                || satietyLevel.contains("healthy")
                || foodDescription.contains("rau")
                || foodDescription.contains("salad")
                || foodDescription.contains("healthy")) {
            return "Lành mạnh";
        }

        return "Nuông chiều";
    }
}
