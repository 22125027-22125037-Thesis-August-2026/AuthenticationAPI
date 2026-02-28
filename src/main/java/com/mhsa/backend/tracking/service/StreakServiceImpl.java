package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;
import com.mhsa.backend.tracking.entity.Streak;
import com.mhsa.backend.tracking.mapper.StreakMapper;
import com.mhsa.backend.tracking.repository.StreakRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StreakServiceImpl implements StreakService {

    private static final String DEFAULT_STREAK_TYPE = "DAILY_TRACKING";

    private final StreakRepository streakRepository;
    private final StreakMapper streakMapper;

    @Override
    @Transactional
    public StreakResponse create(StreakRequest request) {
        if (request == null || request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        Streak entityToSave = streakMapper.toEntity(request);
        if (entityToSave.getStreakType() == null || entityToSave.getStreakType().isBlank()) {
            entityToSave.setStreakType(DEFAULT_STREAK_TYPE);
        }
        if (entityToSave.getCurrentCount() == null) {
            entityToSave.setCurrentCount(0);
        }
        if (entityToSave.getLongestCount() == null) {
            entityToSave.setLongestCount(entityToSave.getCurrentCount());
        }

        Streak savedEntity = streakRepository.save(entityToSave);
        return streakMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<StreakResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return streakRepository.findByProfileId(profileId)
                .map(streakMapper::toResponseDTO)
                .map(List::of)
                .orElseGet(List::of);
    }

    @Override
    public StreakResponse getByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        Streak streak = streakRepository.findByProfileId(profileId)
                .orElseGet(() -> Streak.builder()
                .profileId(profileId)
                .streakType(DEFAULT_STREAK_TYPE)
                .currentCount(0)
                .longestCount(0)
                .build());

        return streakMapper.toResponseDTO(streak);
    }

    @Override
    @Transactional
    public StreakResponse updateStreak(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        Streak streak = streakRepository.findByProfileId(profileId)
                .orElseGet(() -> Streak.builder()
                .profileId(profileId)
                .streakType(DEFAULT_STREAK_TYPE)
                .currentCount(0)
                .longestCount(0)
                .build());

        LocalDateTime lastLoggedAt = streak.getLastLoggedAt();
        if (lastLoggedAt == null || lastLoggedAt.toLocalDate().isBefore(yesterday)) {
            streak.setCurrentCount(1);
        } else if (lastLoggedAt.toLocalDate().isEqual(yesterday)) {
            streak.setCurrentCount(streak.getCurrentCount() + 1);
        }

        if (streak.getLongestCount() == null || streak.getCurrentCount() > streak.getLongestCount()) {
            streak.setLongestCount(streak.getCurrentCount());
        }

        streak.setLastLoggedAt(now);
        Streak savedEntity = streakRepository.save(streak);
        return streakMapper.toResponseDTO(savedEntity);
    }
}
