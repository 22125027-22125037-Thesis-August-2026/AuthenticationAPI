package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.FoodLogRequest;
import com.mhsa.backend.tracking.dto.FoodLogResponse;

public interface FoodLogService {

    FoodLogResponse create(UUID profileId, FoodLogRequest request);

    List<FoodLogResponse> getAllByProfile(UUID profileId);

    List<FoodLogResponse> getFoodEntries(UUID profileId, LocalDate startDate, LocalDate endDate);

    FoodLogResponse getById(UUID profileId, UUID id);

    FoodLogResponse update(UUID profileId, UUID id, FoodLogRequest request);

    void delete(UUID profileId, UUID id);
}
