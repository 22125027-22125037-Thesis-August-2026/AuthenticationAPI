package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;

public interface DiaryEntryService {

    DiaryEntryResponse create(DiaryEntryRequest request);

    List<DiaryEntryResponse> getAllByProfileId(UUID profileId);
}
