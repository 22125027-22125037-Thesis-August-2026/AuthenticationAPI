package com.mhsa.backend.tracking.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public SleepLogResponse create(SleepLogRequest request) {
        if (request == null || request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        SleepLog entityToSave = sleepLogMapper.toEntity(request);
        if (entityToSave.getSleepStartAt() != null && entityToSave.getSleepEndAt() != null) {
            long minutes = Duration.between(entityToSave.getSleepStartAt(), entityToSave.getSleepEndAt()).toMinutes();
            entityToSave.setDurationMinutes((int) Math.max(minutes, 0));
        }

        SleepLog savedEntity = sleepLogRepository.save(entityToSave);
        streakService.updateStreak(request.getProfileId());

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
}
