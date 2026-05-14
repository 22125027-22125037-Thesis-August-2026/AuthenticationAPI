package com.mhsa.backend.tracking.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDiaryEntryCreated(UUID profileId, UUID entryId) {
        String message = String.format("{\"profileId\":\"%s\",\"entryId\":\"%s\"}", profileId, entryId);
        rabbitTemplate.convertAndSend("tracking.diary.created", message);
        log.info("Published diary.created event: profileId={}, entryId={}", profileId, entryId);
    }

    public void publishMoodLogged(UUID profileId, int moodScore) {
        String message = String.format("{\"profileId\":\"%s\",\"moodScore\":%d}", profileId, moodScore);
        rabbitTemplate.convertAndSend("tracking.mood.logged", message);
        log.info("Published mood.logged event: profileId={}, moodScore={}", profileId, moodScore);
    }

    public void publishStreakUpdated(UUID profileId, String streakType) {
        String message = String.format("{\"profileId\":\"%s\",\"streakType\":\"%s\"}", profileId, streakType);
        rabbitTemplate.convertAndSend("tracking.streak.updated", message);
        log.info("Published streak.updated event: profileId={}, streakType={}", profileId, streakType);
    }

    public void publishSleepLogged(UUID profileId, int durationMinutes) {
        String message = String.format("{\"profileId\":\"%s\",\"durationMinutes\":%d}", profileId, durationMinutes);
        rabbitTemplate.convertAndSend("tracking.sleep.logged", message);
        log.info("Published sleep.logged event: profileId={}, durationMinutes={}", profileId, durationMinutes);
    }

    public void publishFoodLogged(UUID profileId) {
        String message = String.format("{\"profileId\":\"%s\"}", profileId);
        rabbitTemplate.convertAndSend("tracking.food.logged", message);
        log.info("Published food.logged event: profileId={}", profileId);
    }
}
