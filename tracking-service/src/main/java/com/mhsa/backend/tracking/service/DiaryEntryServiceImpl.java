package com.mhsa.backend.tracking.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.mapper.DiaryEntryMapper;
import com.mhsa.backend.tracking.messaging.TrackingEventPublisher;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.MediaAttachmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryEntryServiceImpl implements DiaryEntryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final DiaryEntryMapper diaryEntryMapper;
    private final StreakService streakService;
    private final TrackingEventPublisher trackingEventPublisher;
    // Shared object-storage service (see its class comment) — uploads diary attachments to
    // the same MinIO/S3 bucket treasure media already uses.
    private final TreasureStorageService mediaStorageService;

    @Override
    @Transactional
    @CacheEvict(value = "context", key = "#profileId.toString() + '_7'", beforeInvocation = false)
    public DiaryEntryResponse create(UUID profileId, DiaryEntryRequest request, List<MultipartFile> files) {
        if (request == null || profileId == null) {
            log.warn("Create diary entry rejected: profileId or request is null. profileId={}", profileId);
            throw new IllegalArgumentException("profileId is required");
        }

        int attachmentCount = files == null ? 0 : files.size();
        log.info("Creating diary entry for profileId={} with {} attachment(s)", profileId, attachmentCount);

        // 1) Save diary metadata/content first.
        DiaryEntry entityToSave = diaryEntryMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        entityToSave.setEntryDate(resolveEntryDate(request));
        entityToSave.setContent(encrypt(request.getContent()));

        DiaryEntry savedEntity = diaryEntryRepository.save(entityToSave);

        // 2) Save linked attachment records in the same transaction.
        if (files != null && !files.isEmpty()) {
            List<MediaAttachment> attachmentsToSave = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> buildAttachment(profileId, savedEntity, file))
                    .toList();

            List<MediaAttachment> savedAttachments = mediaAttachmentRepository.saveAll(attachmentsToSave);
            savedEntity.getMediaAttachments().addAll(savedAttachments);
            log.info("Saved {} attachment record(s) for diaryEntryId={}", savedAttachments.size(), savedEntity.getId());
        }

        // 3) Update streak and publish event.
        streakService.updateStreak(profileId);
        trackingEventPublisher.publishDiaryEntryCreated(profileId, savedEntity.getId());
        log.info("Streak updated for profileId={} after diary entry creation", profileId);

        DiaryEntryResponse response = diaryEntryMapper.toResponseDTO(savedEntity);
        response.setContent(decrypt(savedEntity.getContent()));
        log.info("Diary entry created successfully. profileId={}, diaryEntryId={}", profileId, savedEntity.getId());
        return response;
    }

    @Override
    public List<DiaryEntryResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return diaryEntryRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(entity -> {
                    DiaryEntryResponse response = diaryEntryMapper.toResponseDTO(entity);
                    response.setContent(decrypt(entity.getContent()));
                    return response;
                })
                .toList();
    }

    @Override
    public DiaryEntryResponse getById(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        DiaryEntry entry = findOwnedDiaryEntry(profileId, id);
        DiaryEntryResponse response = diaryEntryMapper.toResponseDTO(entry);
        response.setContent(decrypt(entry.getContent()));
        return response;
    }

    @Override
    @Transactional
    public DiaryEntryResponse update(UUID profileId, UUID id, DiaryEntryRequest request, List<MultipartFile> files) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }
        if (request == null && (files == null || files.isEmpty())) {
            throw new IllegalArgumentException("request or attachments are required");
        }

        DiaryEntry existing = findOwnedDiaryEntry(profileId, id);

        if (request != null) {
            if (request.getContent() != null && request.getContent().isBlank()) {
                throw new IllegalArgumentException("content must not be blank");
            }
            if (request.getTitle() != null && request.getTitle().isBlank()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            if (request.getContent() != null) {
                existing.setContent(encrypt(request.getContent()));
            }
            if (request.getTitle() != null) {
                existing.setTitle(request.getTitle());
            }
            existing.setMoodTag(request.getMoodTag());
            existing.setPositivityScore(request.getPositivityScore());
            existing.setEntryDate(resolveEntryDate(request));
        }

        if (files != null) {
            // Additive: new attachments are appended, existing ones are left untouched. Removing
            // a specific attachment is a separate call — DELETE /api/v1/tracking/media/{id} —
            // made by the client before/alongside this update, not implied by omission here.
            List<MediaAttachment> attachmentsToSave = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> buildAttachment(profileId, existing, file))
                    .toList();

            if (!attachmentsToSave.isEmpty()) {
                List<MediaAttachment> savedAttachments = mediaAttachmentRepository.saveAll(attachmentsToSave);
                existing.getMediaAttachments().addAll(savedAttachments);
            }
        }

        DiaryEntry savedEntity = diaryEntryRepository.save(existing);
        DiaryEntryResponse response = diaryEntryMapper.toResponseDTO(savedEntity);
        response.setContent(decrypt(savedEntity.getContent()));
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        DiaryEntry existing = findOwnedDiaryEntry(profileId, id);

        // Best-effort media cleanup: never let a failed object delete block the row delete.
        for (MediaAttachment attachment : existing.getMediaAttachments()) {
            deleteAttachmentObject(attachment);
        }

        diaryEntryRepository.delete(existing);
    }

    private DiaryEntry findOwnedDiaryEntry(UUID profileId, UUID id) {
        return diaryEntryRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary entry not found"));
    }

    /**
     * Uploads one multipart file to object storage under this diary entry's owner and builds
     * the (unsaved) MediaAttachment row pointing at it.
     */
    private MediaAttachment buildAttachment(UUID profileId, DiaryEntry diaryEntry, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        log.debug("Uploading diary attachment: profileId={}, fileName={}, mimeType={}", profileId, fileName, fileType);

        String objectKey;
        try {
            objectKey = mediaStorageService.uploadObject(
                    "diary/" + profileId,
                    file.getInputStream(),
                    fileName,
                    fileType);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded attachment", e);
        }

        return MediaAttachment.builder()
                .profileId(profileId)
                .diaryEntry(diaryEntry)
                .fileName(fileName)
                .mimeType(fileType)
                .fileUrl(objectKey)
                .fileSizeBytes(file.getSize())
                .mediaType(resolveMediaType(fileType))
                .build();
    }

    /** Deletes an attachment's backing object, tolerating legacy rows that never had one. */
    private void deleteAttachmentObject(MediaAttachment attachment) {
        String objectKey = attachment.getFileUrl();
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/files/")) {
            return;
        }
        try {
            mediaStorageService.deleteObject(objectKey);
        } catch (Exception e) {
            log.warn("Failed to delete diary attachment object {} for attachmentId={}; deleting row anyway",
                    objectKey, attachment.getId(), e);
        }
    }

    private LocalDate resolveEntryDate(DiaryEntryRequest request) {
        if (request == null || request.getEntryDate() == null) {
            return LocalDate.now();
        }
        return request.getEntryDate();
    }

    private String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    private String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return cipherText;
        }
    }

    private MediaAttachment.MediaType resolveMediaType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return MediaAttachment.MediaType.OTHER;
        }
        if (mimeType.startsWith("image/")) {
            return MediaAttachment.MediaType.IMAGE;
        }
        if (mimeType.startsWith("video/")) {
            return MediaAttachment.MediaType.VIDEO;
        }
        if (mimeType.startsWith("audio/")) {
            return MediaAttachment.MediaType.AUDIO;
        }
        return MediaAttachment.MediaType.OTHER;
    }
}
