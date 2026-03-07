package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;
import com.mhsa.backend.tracking.entity.MoodLog;
import com.mhsa.backend.tracking.mapper.MoodLogMapper;
import com.mhsa.backend.tracking.repository.MoodLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoodLogServiceImpl implements MoodLogService {

    private final MoodLogRepository moodLogRepository;
    private final MoodLogMapper moodLogMapper;
    private final StreakService streakService;

    @Override
    @Transactional
    public MoodLogResponse create(UUID profileId, MoodLogRequest request) {
        if (request == null || profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        MoodLog entityToSave = moodLogMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        MoodLog savedEntity = moodLogRepository.save(entityToSave);
        streakService.updateStreak(profileId);

        return moodLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<MoodLogResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return moodLogRepository.findByProfileIdOrderByLogDateDesc(profileId)
                .stream()
                .map(moodLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    public MoodLogResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        return moodLogMapper.toResponseDTO(findOwnedMoodLog(profileId, id));
    }

    @Override
    @Transactional
    public MoodLogResponse update(UUID profileId, UUID id, MoodLogRequest request) {
        if (profileId == null || id == null || request == null) {
            throw new IllegalArgumentException("profileId, id and request are required");
        }

        MoodLog existing = findOwnedMoodLog(profileId, id);
        existing.setMoodScore(request.getPositivityScore());
        existing.setNote(request.getNote());

        MoodLog savedEntity = moodLogRepository.save(existing);
        return moodLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        MoodLog existing = findOwnedMoodLog(profileId, id);
        moodLogRepository.delete(existing);
    }

    private MoodLog findOwnedMoodLog(UUID profileId, UUID id) {
        return moodLogRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mood log not found"));
    }
}
