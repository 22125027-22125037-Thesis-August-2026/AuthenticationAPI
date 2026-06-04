package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.StepLogRequest;
import com.mhsa.backend.tracking.dto.StepLogResponse;

public interface StepLogService {

    StepLogResponse upsert(UUID profileId, StepLogRequest request);

    List<StepLogResponse> getAllByProfileId(UUID profileId);

    List<StepLogResponse> getByDateRange(UUID profileId, LocalDate from, LocalDate to);

    void delete(UUID profileId, UUID id);
}
