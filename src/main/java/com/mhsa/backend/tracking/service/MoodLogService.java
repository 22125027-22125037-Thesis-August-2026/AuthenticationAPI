package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;

public interface MoodLogService {

    MoodLogResponse create(MoodLogRequest request);

    List<MoodLogResponse> getAllByProfileId(UUID profileId);
}
