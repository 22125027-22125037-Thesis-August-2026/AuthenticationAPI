package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public StreakResponse create(UUID profileId, StreakRequest request) {
        if (request == null || profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        Streak entityToSave = streakMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
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
    public StreakResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        return streakMapper.toResponseDTO(findOwnedStreak(profileId, id));
    }

    @Override
    @Transactional
    public StreakResponse update(UUID profileId, UUID id, StreakRequest request) {
        if (profileId == null || id == null || request == null) {
            throw new IllegalArgumentException("profileId, id and request are required");
        }

        Streak existing = findOwnedStreak(profileId, id);
        existing.setStreakType(request.getStreakType());
        existing.setCurrentCount(request.getCurrentCount());
        existing.setLongestCount(request.getLongestCount());
        existing.setLastLoggedAt(request.getLastLoggedAt());

        if (existing.getStreakType() == null || existing.getStreakType().isBlank()) {
            existing.setStreakType(DEFAULT_STREAK_TYPE);
        }
        if (existing.getCurrentCount() == null) {
            existing.setCurrentCount(0);
        }
        if (existing.getLongestCount() == null) {
            existing.setLongestCount(existing.getCurrentCount());
        }

        Streak savedEntity = streakRepository.save(existing);
        return streakMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        Streak existing = findOwnedStreak(profileId, id);
        streakRepository.delete(existing);
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

    private Streak findOwnedStreak(UUID profileId, UUID id) {
        return streakRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Streak not found"));
    }
}
