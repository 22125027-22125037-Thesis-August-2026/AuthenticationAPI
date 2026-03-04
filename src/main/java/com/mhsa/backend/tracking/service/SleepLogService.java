package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.SleepLogRequest;
import com.mhsa.backend.tracking.dto.SleepLogResponse;

public interface SleepLogService {

    SleepLogResponse create(UUID profileId, SleepLogRequest request);

    List<SleepLogResponse> getAllByProfileId(UUID profileId);
}
