package com.mhsa.backend.tracking.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.FoodLogRepository;
import com.mhsa.backend.tracking.repository.MediaAttachmentRepository;
import com.mhsa.backend.tracking.repository.MoodLogRepository;
import com.mhsa.backend.tracking.repository.SleepLogRepository;
import com.mhsa.backend.tracking.repository.StreakRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListener {

    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final FoodLogRepository foodLogRepository;
    private final MoodLogRepository moodLogRepository;
    private final SleepLogRepository sleepLogRepository;
    private final StreakRepository streakRepository;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "auth.user.deleted")
    @Transactional
    public void onUserDeleted(String event) {
        try {
            log.info("User deleted event received: {}", event);
            JsonNode node = objectMapper.readTree(event);
            UUID profileId = UUID.fromString(node.get("userId").asText());

            mediaAttachmentRepository.deleteByProfileId(profileId);
            diaryEntryRepository.deleteByProfileId(profileId);
            foodLogRepository.deleteByProfileId(profileId);
            moodLogRepository.deleteByProfileId(profileId);
            sleepLogRepository.deleteByProfileId(profileId);
            streakRepository.deleteByProfileId(profileId);

            if (cacheManager.getCache("context") != null) {
                cacheManager.getCache("context").evict(profileId + "_7");
            }

            log.info("User data deleted successfully: profileId={}", profileId);
        } catch (Exception e) {
            log.error("Error processing user deleted event: {}", event, e);
        }
    }

    @RabbitListener(queues = "auth.user.updated")
    public void onUserUpdated(String event) {
        try {
            log.info("User updated event received: {}", event);
            JsonNode node = objectMapper.readTree(event);
            UUID profileId = UUID.fromString(node.get("userId").asText());

            if (cacheManager.getCache("context") != null) {
                cacheManager.getCache("context").evict(profileId + "_7");
            }

            log.info("User cache invalidated: profileId={}", profileId);
        } catch (Exception e) {
            log.error("Error processing user updated event: {}", event, e);
        }
    }

    @RabbitListener(queues = "auth.grant.created")
    public void onGrantCreated(String event) {
        try {
            log.info("Auth grant created event received: {}", event);
            log.debug("Grant details: {}", event);
        } catch (Exception e) {
            log.error("Error processing grant created event: {}", event, e);
        }
    }
}
