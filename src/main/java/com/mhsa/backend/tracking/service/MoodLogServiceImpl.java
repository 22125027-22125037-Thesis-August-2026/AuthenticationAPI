package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
