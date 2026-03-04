package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.mapper.MediaAttachmentMapper;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.MediaAttachmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaAttachmentServiceImpl implements MediaAttachmentService {

    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final MediaAttachmentMapper mediaAttachmentMapper;

    @Override
    @Transactional
    public MediaAttachmentResponse create(UUID profileId, MediaAttachmentRequest request) {
        if (request == null || profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        MediaAttachment entityToSave = mediaAttachmentMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        if (request.getReferenceId() != null
                && request.getReferenceType() != null
                && "DIARY_ENTRY".equalsIgnoreCase(request.getReferenceType())) {
            DiaryEntry diaryEntry = diaryEntryRepository.findById(request.getReferenceId())
                    .orElseThrow(() -> new IllegalArgumentException("DiaryEntry not found: " + request.getReferenceId()));
            entityToSave.setDiaryEntry(diaryEntry);
            entityToSave.setFoodLogId(null);
        }

        if (request.getReferenceId() != null
                && request.getReferenceType() != null
                && "FOOD_LOG".equalsIgnoreCase(request.getReferenceType())) {
            entityToSave.setFoodLogId(request.getReferenceId());
            entityToSave.setDiaryEntry(null);
        }

        MediaAttachment savedEntity = mediaAttachmentRepository.save(entityToSave);
        return mediaAttachmentMapper.toResponseDTO(savedEntity);
    }

    @Override
    public List<MediaAttachmentResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return mediaAttachmentRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(mediaAttachmentMapper::toResponseDTO)
                .toList();
    }
}
