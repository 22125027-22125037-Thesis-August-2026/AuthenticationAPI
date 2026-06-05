package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.BreathingLogRequest;
import com.mhsa.backend.tracking.dto.BreathingLogResponse;

public interface BreathingLogService {

    BreathingLogResponse upsert(UUID profileId, BreathingLogRequest request);

    List<BreathingLogResponse> getAllByProfileId(UUID profileId);

    List<BreathingLogResponse> getByDateRange(UUID profileId, LocalDate from, LocalDate to);

    void delete(UUID profileId, UUID id);
}
