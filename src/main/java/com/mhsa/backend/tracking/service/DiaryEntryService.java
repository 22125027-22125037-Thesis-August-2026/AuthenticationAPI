package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;

public interface DiaryEntryService {

    DiaryEntryResponse create(UUID profileId, DiaryEntryRequest request, List<MultipartFile> files);

    List<DiaryEntryResponse> getAllByProfileId(UUID profileId);

    DiaryEntryResponse getById(UUID profileId, UUID id);

    DiaryEntryResponse update(UUID profileId, UUID id, DiaryEntryRequest request, List<MultipartFile> files);

    void delete(UUID profileId, UUID id);
}
