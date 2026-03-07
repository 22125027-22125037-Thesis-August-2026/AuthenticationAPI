package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.FoodLogRequest;
import com.mhsa.backend.tracking.dto.FoodLogResponse;
import com.mhsa.backend.tracking.entity.FoodLog;
import com.mhsa.backend.tracking.mapper.FoodLogMapper;
import com.mhsa.backend.tracking.repository.FoodLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodLogServiceImpl implements FoodLogService {

    private final FoodLogRepository foodLogRepository;
    private final FoodLogMapper foodLogMapper;

    @Override
    @Transactional
    public FoodLogResponse create(UUID profileId, FoodLogRequest request) {
        if (profileId == null || request == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        FoodLog entityToSave = foodLogMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        FoodLog savedEntity = foodLogRepository.save(entityToSave);
        return foodLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<FoodLogResponse> getAllByProfile(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return foodLogRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(foodLogMapper::toResponseDTO)
                .toList();
    }

    @Override
    public FoodLogResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        return foodLogMapper.toResponseDTO(findOwnedFoodLog(profileId, id));
    }

    @Override
    @Transactional
    public FoodLogResponse update(UUID profileId, UUID id, FoodLogRequest request) {
        if (profileId == null || id == null || request == null) {
            throw new IllegalArgumentException("profileId, id and request are required");
        }

        FoodLog existing = findOwnedFoodLog(profileId, id);
        existing.setMealType(request.getMealType());
        existing.setFoodDescription(request.getFoodDescription());
        existing.setSatietyLevel(request.getSatietyLevel());

        FoodLog savedEntity = foodLogRepository.save(existing);
        return foodLogMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        FoodLog existing = findOwnedFoodLog(profileId, id);
        foodLogRepository.delete(existing);
    }

    private FoodLog findOwnedFoodLog(UUID profileId, UUID id) {
        return foodLogRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food log not found"));
    }
}
