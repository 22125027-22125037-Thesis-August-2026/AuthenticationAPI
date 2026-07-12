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

import com.mhsa.backend.tracking.dto.StepLogRequest;
import com.mhsa.backend.tracking.dto.StepLogResponse;
import com.mhsa.backend.tracking.entity.StepLog;
import com.mhsa.backend.tracking.mapper.StepLogMapper;
import com.mhsa.backend.tracking.repository.StepLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StepLogServiceImpl implements StepLogService {

    private static final int DEFAULT_GOAL = 6000;
    private static final String DEFAULT_SOURCE = "DEVICE_SENSOR";

    private final StepLogRepository stepLogRepository;
    private final StepLogMapper stepLogMapper;

    @Override
    @Transactional
    @CacheEvict(value = "context", key = "#profileId.toString() + '_7'", beforeInvocation = false)
    public StepLogResponse upsert(UUID profileId, StepLogRequest request) {
        if (profileId == null || request == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        LocalDate entryDate = resolveEntryDate(request);
        StepLog entityToSave = stepLogRepository.findByProfileIdAndEntryDate(profileId, entryDate)
                .orElseGet(StepLog::new);

        applyRequestToEntity(entityToSave, request, profileId, entryDate);

        StepLog savedEntity = stepLogRepository.save(entityToSave);
        return stepLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<StepLogResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return stepLogRepository.findByProfileIdOrderByEntryDateDesc(profileId)
                .stream()
                .map(stepLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<StepLogResponse> getByDateRange(UUID profileId, LocalDate from, LocalDate to) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        return stepLogRepository.findByProfileIdAndEntryDateBetween(profileId, from, to)
                .stream()
                .map(stepLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        StepLog existing = findOwnedStepLog(profileId, id);
        stepLogRepository.delete(existing);
    }

    private StepLog findOwnedStepLog(UUID profileId, UUID id) {
        return stepLogRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step log not found"));
    }

    private void applyRequestToEntity(StepLog target, StepLogRequest request, UUID profileId, LocalDate entryDate) {
        target.setProfileId(profileId);
        target.setEntryDate(entryDate);
        target.setStepCount(request.getStepCount());
        target.setGoal(Objects.requireNonNullElse(request.getGoal(), DEFAULT_GOAL));
        target.setSource(Objects.requireNonNullElse(request.getSource(), DEFAULT_SOURCE));
        target.setLoggedAt(LocalDateTime.now());
    }

    private LocalDate resolveEntryDate(StepLogRequest request) {
        if (request == null || request.getEntryDate() == null) {
            return LocalDate.now();
        }
        return request.getEntryDate();
    }
}
