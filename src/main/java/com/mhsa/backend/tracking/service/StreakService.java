package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;

public interface StreakService {

    StreakResponse create(UUID profileId, StreakRequest request);

    List<StreakResponse> getAllByProfileId(UUID profileId);

    StreakResponse getByProfileId(UUID profileId);

    StreakResponse getById(UUID profileId, UUID id);

    StreakResponse update(UUID profileId, UUID id, StreakRequest request);

    void delete(UUID profileId, UUID id);

    StreakResponse updateStreak(UUID profileId);
}
