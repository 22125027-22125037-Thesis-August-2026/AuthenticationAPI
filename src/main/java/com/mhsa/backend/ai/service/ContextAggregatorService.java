package com.mhsa.backend.ai.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.FoodLog;
import com.mhsa.backend.tracking.entity.SleepLog;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.FoodLogRepository;
import com.mhsa.backend.tracking.repository.SleepLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContextAggregatorService {

    private final SleepLogRepository sleepLogRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final FoodLogRepository foodLogRepository;

    public String getUserContextSummary(UUID profileId) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        // --- Sleep ---
        List<SleepLog> sleepLogs = sleepLogRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .filter(log -> log.getCreatedAt() != null && log.getCreatedAt().toLocalDate().isAfter(sevenDaysAgo.minusDays(1)))
                .collect(Collectors.toList());
        double avgHours = sleepLogs.stream()
                .filter(log -> log.getDurationMinutes() != null)
                .mapToInt(SleepLog::getDurationMinutes)
                .average()
                .orElse(0.0) / 60.0;
        long poorSleepDays = sleepLogs.stream()
                .filter(log -> log.getSleepQuality() != null && log.getSleepQuality() <= 2)
                .count();

        // --- Diary ---
        List<DiaryEntry> diaryEntries = diaryEntryRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .filter(entry -> entry.getCreatedAt() != null && entry.getCreatedAt().toLocalDate().isAfter(sevenDaysAgo.minusDays(1)))
                .collect(Collectors.toList());
        Set<String> dominantEmotions = diaryEntries.stream()
                .map(DiaryEntry::getMoodTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // --- Food ---
        List<FoodLog> foodLogs = foodLogRepository.findByProfileIdOrderByEntryDateDesc(profileId)
                .stream()
                .filter(log -> log.getEntryDate() != null && log.getEntryDate().isAfter(sevenDaysAgo.minusDays(1)))
                .collect(Collectors.toList());
        long lowWaterDays = foodLogs.stream()
                .filter(log -> log.getWaterGlasses() != null && log.getWaterGlasses() < 6)
                .count();
        long skippedMeals = foodLogs.stream()
                .filter(log -> log.getFoodDescription() != null && log.getFoodDescription().toLowerCase().contains("skipped"))
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("[USER CONTEXT - LAST 7 DAYS]\n");
        sb.append("- Sleep: Averaging ")
                .append(String.format("%.1f", avgHours)).append(" hours. ");
        if (poorSleepDays > 0) {
            sb.append("Note: ").append(poorSleepDays).append(" days of \"Poor\" sleep. ");
        }
        sb.append("\n");
        sb.append("- Mood: Dominant emotion is ");
        if (!dominantEmotions.isEmpty()) {
            sb.append(String.join(" and ", dominantEmotions));
        } else {
            sb.append("N/A");
        }
        sb.append(".\n");
        sb.append("- Diet: Skipped breakfast ").append(skippedMeals).append(" times.\n");
        sb.append("- Hydration: ").append(lowWaterDays).append(" days below 6 glasses.\n");
        return sb.toString();
    }
}
