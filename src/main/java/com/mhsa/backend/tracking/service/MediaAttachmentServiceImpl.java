package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.Locale;
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
    public MediaAttachmentResponse create(MediaAttachmentRequest request) {
        if (request == null || request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        MediaAttachment entityToSave = mediaAttachmentMapper.toEntity(request);
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
        MediaAttachmentResponse response = mediaAttachmentMapper.toResponseDTO(savedEntity);
        if (response.getReferenceType() == null && request.getReferenceType() != null) {
            response.setReferenceType(request.getReferenceType().toUpperCase(Locale.ROOT));
            response.setReferenceId(request.getReferenceId());
        }

        return response;
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
