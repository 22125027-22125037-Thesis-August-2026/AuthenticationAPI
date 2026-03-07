package com.mhsa.backend.tracking.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.SleepLogRequest;
import com.mhsa.backend.tracking.dto.SleepLogResponse;
import com.mhsa.backend.tracking.entity.SleepLog;
import com.mhsa.backend.tracking.mapper.SleepLogMapper;
import com.mhsa.backend.tracking.repository.SleepLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SleepLogServiceImpl implements SleepLogService {

    private final SleepLogRepository sleepLogRepository;
    private final SleepLogMapper sleepLogMapper;
    private final StreakService streakService;

    @Override
    @Transactional
    public SleepLogResponse create(UUID profileId, SleepLogRequest request) {
        if (request == null || profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        SleepLog entityToSave = sleepLogMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        if (entityToSave.getSleepStartAt() != null && entityToSave.getSleepEndAt() != null) {
            long minutes = Duration.between(entityToSave.getSleepStartAt(), entityToSave.getSleepEndAt()).toMinutes();
            entityToSave.setDurationMinutes((int) Math.max(minutes, 0));
        }

        SleepLog savedEntity = sleepLogRepository.save(entityToSave);
        streakService.updateStreak(profileId);

        return sleepLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<SleepLogResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return sleepLogRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(sleepLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    public SleepLogResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        return sleepLogMapper.toResponseDTO(findOwnedSleepLog(profileId, id));
    }

    @Override
    @Transactional
    public SleepLogResponse update(UUID profileId, UUID id, SleepLogRequest request) {
        if (profileId == null || id == null || request == null) {
            throw new IllegalArgumentException("profileId, id and request are required");
        }

        SleepLog existing = findOwnedSleepLog(profileId, id);
        existing.setSleepStartAt(request.getBedTime());
        existing.setSleepEndAt(request.getWakeTime());
        existing.setSleepQuality(request.getSleepQuality());
        existing.setNote(request.getNote());

        if (existing.getSleepStartAt() != null && existing.getSleepEndAt() != null) {
            long minutes = Duration.between(existing.getSleepStartAt(), existing.getSleepEndAt()).toMinutes();
            existing.setDurationMinutes((int) Math.max(minutes, 0));
        } else {
            existing.setDurationMinutes(null);
        }

        SleepLog savedEntity = sleepLogRepository.save(existing);
        return sleepLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        SleepLog existing = findOwnedSleepLog(profileId, id);
        sleepLogRepository.delete(existing);
    }

    private SleepLog findOwnedSleepLog(UUID profileId, UUID id) {
        return sleepLogRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleep log not found"));
    }
}
