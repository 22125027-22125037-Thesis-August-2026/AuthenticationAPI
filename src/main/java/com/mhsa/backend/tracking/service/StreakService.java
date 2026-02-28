package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;

public interface StreakService {

    StreakResponse create(StreakRequest request);

    List<StreakResponse> getAllByProfileId(UUID profileId);

    StreakResponse getByProfileId(UUID profileId);

    StreakResponse updateStreak(UUID profileId);
}
