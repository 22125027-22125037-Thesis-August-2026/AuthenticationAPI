package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;

public interface MediaAttachmentService {

    MediaAttachmentResponse create(UUID profileId, MediaAttachmentRequest request);

    List<MediaAttachmentResponse> getAllByProfileId(UUID profileId);

    MediaAttachmentResponse getById(UUID profileId, UUID id);

    MediaAttachmentResponse update(UUID profileId, UUID id, MediaAttachmentRequest request);

    void delete(UUID profileId, UUID id);
}
