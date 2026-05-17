package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.FoodLog;
import com.mhsa.backend.tracking.entity.SleepLog;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.FoodLogRepository;
import com.mhsa.backend.tracking.repository.SleepLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextAggregatorService {

    private final SleepLogRepository sleepLogRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final FoodLogRepository foodLogRepository;

    @Cacheable(value = "context", key = "#profileId.toString() + '_' + #days")
    public String getUserContextSummary(UUID profileId, int days) {
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);

            List<SleepLog> sleepLogs = sleepLogRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                    .stream()
                    .filter(log -> log.getEntryDate() != null && !log.getEntryDate().isBefore(startDate))
                    .toList();

            List<DiaryEntry> diaryEntries = diaryEntryRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                    .stream()
                    .filter(entry -> entry.getEntryDate() != null && !entry.getEntryDate().isBefore(startDate))
                    .toList();

            List<FoodLog> foodLogs = foodLogRepository.findByProfileIdOrderByEntryDateDesc(profileId)
                    .stream()
                    .filter(log -> log.getEntryDate() != null && !log.getEntryDate().isBefore(startDate))
                    .toList();

            StringBuilder context = new StringBuilder();
            context.append("[USER CONTEXT - LAST ").append(days).append(" DAYS]\n");

            // Sleep context
            if (!sleepLogs.isEmpty()) {
                double avgSleepHours = sleepLogs.stream()
                        .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
                        .mapToDouble(log -> log.getDurationMinutes() / 60.0)
                        .average()
                        .orElse(0.0);

                long poorSleepDays = sleepLogs.stream()
                        .filter(log -> log.getSleepQuality() != null && log.getSleepQuality() <= 2)
                        .count();

                context.append("- Sleep: Average ").append(String.format("%.1f", avgSleepHours))
                        .append(" hours/night. Poor sleep days: ").append(poorSleepDays).append("/").append(days).append(".\n");
            } else {
                context.append("- Sleep: No sleep data available.\n");
            }

            // Mood context
            if (!diaryEntries.isEmpty()) {
                Set<String> uniqueMoods = diaryEntries.stream()
                        .map(DiaryEntry::getMoodTag)
                        .filter(tag -> tag != null && !tag.isBlank())
                        .collect(Collectors.toSet());

                if (!uniqueMoods.isEmpty()) {
                    context.append("- Mood: Recent emotions: ").append(uniqueMoods).append(".\n");
                } else {
                    context.append("- Mood: No mood tags recorded.\n");
                }
            } else {
                context.append("- Mood: No diary entries.\n");
            }

            // Diet context
            if (!foodLogs.isEmpty()) {
                long lowHydrationDays = foodLogs.stream()
                        .filter(log -> log.getWaterGlasses() < 6)
                        .count();

                long skippedMeals = foodLogs.stream()
                        .filter(log -> log.getFoodDescription() != null &&
                                log.getFoodDescription().toLowerCase().contains("skipped"))
                        .count();

                context.append("- Diet: Low hydration days: ").append(lowHydrationDays).append("/").append(days)
                        .append(". Skipped meals: ").append(skippedMeals).append(" times.\n");
            } else {
                context.append("- Diet: No food logs available.\n");
            }

            return context.toString();
        } catch (Exception e) {
            log.error("Error aggregating context for profileId: {}", profileId, e);
            return buildDefaultContext();
        }
    }

    private String buildDefaultContext() {
        return "[USER CONTEXT - UNAVAILABLE]\n"
                + "- Sleep: Unable to retrieve recent sleep data.\n"
                + "- Mood: Unable to retrieve recent mood data.\n"
                + "- Diet: Unable to retrieve recent diet data.\n";
    }
}
