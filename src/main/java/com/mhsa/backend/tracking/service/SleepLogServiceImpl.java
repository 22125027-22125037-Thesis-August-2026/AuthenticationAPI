package com.mhsa.backend.tracking.service;

import java.time.Duration;
import java.time.LocalDate;
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

        LocalDate entryDate = resolveEntryDate(request);
        SleepLog entityToSave = sleepLogRepository.findByProfileIdAndEntryDate(profileId, entryDate)
                .orElseGet(() -> sleepLogMapper.toEntity(request));

        applyRequestToEntity(entityToSave, request, profileId, entryDate);

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
        LocalDate entryDate = resolveEntryDate(request);

        sleepLogRepository.findByProfileIdAndEntryDate(profileId, entryDate)
                .filter(other -> !other.getId().equals(existing.getId()))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Sleep log already exists for the selected date");
                });

        applyRequestToEntity(existing, request, profileId, entryDate);

        SleepLog savedEntity = sleepLogRepository.save(existing);
        return sleepLogMapper.toResponseDTO(savedEntity);
    }

    private void applyRequestToEntity(SleepLog target, SleepLogRequest request, UUID profileId, LocalDate entryDate) {
        target.setProfileId(profileId);
        target.setEntryDate(entryDate);
        target.setSleepStartAt(request.getBedTime());
        target.setSleepEndAt(request.getWakeTime());
        target.setSleepQuality(request.getSleepQuality());
        target.setNote(request.getNote());

        if (target.getSleepStartAt() != null && target.getSleepEndAt() != null) {
            long minutes = Duration.between(target.getSleepStartAt(), target.getSleepEndAt()).toMinutes();
            target.setDurationMinutes((int) Math.max(minutes, 0));
        } else {
            target.setDurationMinutes(null);
        }
    }

    private LocalDate resolveEntryDate(SleepLogRequest request) {
        if (request == null || request.getEntryDate() == null) {
            return LocalDate.now();
        }
        return request.getEntryDate();
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
