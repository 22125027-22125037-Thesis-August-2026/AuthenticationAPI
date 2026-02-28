package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;

public interface MediaAttachmentService {

    MediaAttachmentResponse create(MediaAttachmentRequest request);

    List<MediaAttachmentResponse> getAllByProfileId(UUID profileId);
}
