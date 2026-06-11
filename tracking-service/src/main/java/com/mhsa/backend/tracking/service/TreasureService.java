package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.tracking.dto.TreasureRequest;
import com.mhsa.backend.tracking.dto.TreasureResponse;

public interface TreasureService {

    TreasureResponse create(UUID profileId, TreasureRequest request, MultipartFile media);

    List<TreasureResponse> getAllByProfileId(UUID profileId);

    void delete(UUID profileId, UUID id);
}
