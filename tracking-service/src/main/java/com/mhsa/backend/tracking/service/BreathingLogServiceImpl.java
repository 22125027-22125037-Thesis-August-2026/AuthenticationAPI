package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.BreathingLogRequest;
import com.mhsa.backend.tracking.dto.BreathingLogResponse;
import com.mhsa.backend.tracking.entity.BreathingLog;
import com.mhsa.backend.tracking.mapper.BreathingLogMapper;
import com.mhsa.backend.tracking.repository.BreathingLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BreathingLogServiceImpl implements BreathingLogService {

    private static final int DEFAULT_GOAL_SECONDS = 300;
    private static final String DEFAULT_SOURCE = "GUIDED_SESSION";

    private final BreathingLogRepository breathingLogRepository;
    private final BreathingLogMapper breathingLogMapper;

    @Override
    @Transactional
    @CacheEvict(value = "context", key = "#profileId.toString() + '_7'", beforeInvocation = false)
    public BreathingLogResponse upsert(UUID profileId, BreathingLogRequest request) {
        if (profileId == null || request == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        LocalDate entryDate = resolveEntryDate(request);
        BreathingLog entityToSave = breathingLogRepository.findByProfileIdAndEntryDate(profileId, entryDate)
                .orElseGet(BreathingLog::new);

        applyRequestToEntity(entityToSave, request, profileId, entryDate);

        BreathingLog savedEntity = breathingLogRepository.save(entityToSave);
        return breathingLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<BreathingLogResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return breathingLogRepository.findByProfileIdOrderByEntryDateDesc(profileId)
                .stream()
                .map(breathingLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<BreathingLogResponse> getByDateRange(UUID profileId, LocalDate from, LocalDate to) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        return breathingLogRepository.findByProfileIdAndEntryDateBetween(profileId, from, to)
                .stream()
                .map(breathingLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        BreathingLog existing = findOwnedBreathingLog(profileId, id);
        breathingLogRepository.delete(existing);
    }

    private BreathingLog findOwnedBreathingLog(UUID profileId, UUID id) {
        return breathingLogRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Breathing log not found"));
    }

    private void applyRequestToEntity(BreathingLog target, BreathingLogRequest request, UUID profileId, LocalDate entryDate) {
        // Accumulate: each session ADDS its duration to the day's total and bumps the session count.
        int currentDuration = Objects.requireNonNullElse(target.getTotalDurationSeconds(), 0);
        int currentSessions = Objects.requireNonNullElse(target.getSessionsCompleted(), 0);

        target.setProfileId(profileId);
        target.setEntryDate(entryDate);
        target.setTotalDurationSeconds(currentDuration + request.getDurationSeconds());
        target.setSessionsCompleted(currentSessions + 1);
        target.setGoalSeconds(Objects.requireNonNullElse(request.getGoalSeconds(), DEFAULT_GOAL_SECONDS));
        target.setSource(Objects.requireNonNullElse(request.getSource(), DEFAULT_SOURCE));
        target.setLoggedAt(LocalDateTime.now());
    }

    private LocalDate resolveEntryDate(BreathingLogRequest request) {
        if (request == null || request.getEntryDate() == null) {
            return LocalDate.now();
        }
        return request.getEntryDate();
    }
}
