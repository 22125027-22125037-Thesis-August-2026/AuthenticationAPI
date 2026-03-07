package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;

public interface MoodLogService {

    MoodLogResponse create(UUID profileId, MoodLogRequest request);

    List<MoodLogResponse> getAllByProfileId(UUID profileId);

    MoodLogResponse getById(UUID profileId, UUID id);

    MoodLogResponse update(UUID profileId, UUID id, MoodLogRequest request);

    void delete(UUID profileId, UUID id);
}
