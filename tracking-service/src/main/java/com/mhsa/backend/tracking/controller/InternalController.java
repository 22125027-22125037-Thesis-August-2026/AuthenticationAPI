package com.mhsa.backend.tracking.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.tracking.repository.MoodLogRepository;
import com.mhsa.backend.tracking.repository.SleepLogRepository;
import com.mhsa.backend.tracking.repository.StreakRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final MoodLogRepository moodLogRepository;
    private final SleepLogRepository sleepLogRepository;
    private final StreakRepository streakRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/{profileId}/summary")
    public ResponseEntity<ObjectNode> getDashboardSummary(@PathVariable UUID profileId) {
        try {
            ObjectNode summary = objectMapper.createObjectNode();

            // Get mood data
            var moodLogs = moodLogRepository.findByProfileIdOrderByLogDateDesc(profileId);
            if (!moodLogs.isEmpty()) {
                summary.put("latestMood", moodLogs.get(0).getMoodScore());
                summary.put("moodCount", moodLogs.size());
            }

            // Get sleep data
            var sleepLogs = sleepLogRepository.findByProfileIdOrderByCreatedAtDesc(profileId);
            if (!sleepLogs.isEmpty()) {
                int avgDuration = (int) sleepLogs.stream()
                        .filter(log -> log.getDurationMinutes() != null)
                        .mapToInt(log -> log.getDurationMinutes())
                        .average()
                        .orElse(0);
                summary.put("avgSleepMinutes", avgDuration);
                summary.put("sleepCount", sleepLogs.size());
            }

            // Get streak data
            var streak = streakRepository.findByProfileId(profileId);
            if (streak.isPresent()) {
                summary.put("currentStreak", streak.get().getCurrentCount());
                summary.put("longestStreak", streak.get().getLongestCount());
            }

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting dashboard summary: profileId={}", profileId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
