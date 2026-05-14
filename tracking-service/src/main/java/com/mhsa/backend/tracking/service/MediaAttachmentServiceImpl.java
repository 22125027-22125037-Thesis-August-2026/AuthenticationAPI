package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        applyReference(entityToSave, request, profileId);

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

    @Override
    public MediaAttachmentResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        return mediaAttachmentMapper.toResponseDTO(findOwnedMediaAttachment(profileId, id));
    }

    @Override
    @Transactional
    public MediaAttachmentResponse update(UUID profileId, UUID id, MediaAttachmentRequest request) {
        if (profileId == null || id == null || request == null) {
            throw new IllegalArgumentException("profileId, id and request are required");
        }

        MediaAttachment existing = findOwnedMediaAttachment(profileId, id);
        existing.setFileUrl(request.getFileUrl());
        existing.setMimeType(request.getMimeType());
        existing.setFileSizeBytes(request.getFileSizeBytes());

        MediaAttachment mapped = mediaAttachmentMapper.toEntity(request);
        existing.setMediaType(mapped.getMediaType());
        applyReference(existing, request, profileId);

        MediaAttachment savedEntity = mediaAttachmentRepository.save(existing);
        return mediaAttachmentMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        MediaAttachment existing = findOwnedMediaAttachment(profileId, id);
        mediaAttachmentRepository.delete(existing);
    }

    private MediaAttachment findOwnedMediaAttachment(UUID profileId, UUID id) {
        return mediaAttachmentRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media attachment not found"));
    }

    private void applyReference(MediaAttachment attachment, MediaAttachmentRequest request, UUID profileId) {
        if (request.getReferenceId() == null || request.getReferenceType() == null) {
            attachment.setDiaryEntry(null);
            attachment.setFoodLogId(null);
            return;
        }

        if ("DIARY_ENTRY".equalsIgnoreCase(request.getReferenceType())) {
            DiaryEntry diaryEntry = diaryEntryRepository.findByIdAndProfileId(request.getReferenceId(), profileId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary entry not found"));
            attachment.setDiaryEntry(diaryEntry);
            attachment.setFoodLogId(null);
            return;
        }

        if ("FOOD_LOG".equalsIgnoreCase(request.getReferenceType())) {
            attachment.setFoodLogId(request.getReferenceId());
            attachment.setDiaryEntry(null);
            return;
        }

        attachment.setDiaryEntry(null);
        attachment.setFoodLogId(null);
    }
}
