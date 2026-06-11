package com.mhsa.backend.tracking.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.TreasureRequest;
import com.mhsa.backend.tracking.dto.TreasureResponse;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.entity.Treasure;
import com.mhsa.backend.tracking.mapper.TreasureMapper;
import com.mhsa.backend.tracking.repository.TreasureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TreasureServiceImpl implements TreasureService {

    // Media is anchored to a single treasure and read inline, so a generous-but-bounded cap.
    private static final long MAX_MEDIA_SIZE_BYTES = 25L * 1024 * 1024; // 25MB
    // Presigned GET URLs are short-lived; the mobile client re-fetches the list to refresh them.
    private static final long PRESIGNED_URL_VALIDITY_SECONDS = 24 * 3600; // 24 hours

    private final TreasureRepository treasureRepository;
    private final TreasureMapper treasureMapper;
    private final TreasureStorageService treasureStorageService;

    @Override
    @Transactional
    public TreasureResponse create(UUID profileId, TreasureRequest request, MultipartFile media) {
        if (profileId == null || request == null) {
            throw new IllegalArgumentException("profileId and request are required");
        }

        log.info("Creating treasure for profileId={}, category={}, hasMedia={}",
                profileId, request.getCategory(), media != null && !media.isEmpty());

        Treasure entity = treasureMapper.toEntity(request);
        entity.setProfileId(profileId);

        if (media != null && !media.isEmpty()) {
            String contentType = media.getContentType();
            MediaAttachment.MediaType mediaType = resolveMediaType(contentType);
            if (media.getSize() > MAX_MEDIA_SIZE_BYTES) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(413),
                        "Media must not exceed 25MB");
            }

            String objectKey;
            try {
                objectKey = treasureStorageService.uploadTreasureMedia(
                        profileId.toString(),
                        media.getInputStream(),
                        media.getOriginalFilename(),
                        contentType);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded media", e);
            }

            entity.setMediaObjectKey(objectKey);
            entity.setMediaType(mediaType);
            entity.setMimeType(contentType);
        }

        Treasure saved = treasureRepository.save(entity);
        log.info("Treasure created successfully. profileId={}, treasureId={}", profileId, saved.getId());
        return toResponseWithUrl(saved);
    }

    @Override
    public List<TreasureResponse> getAllByProfileId(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        return treasureRepository.findByProfileIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(this::toResponseWithUrl)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID profileId, UUID id) {
        if (profileId == null || id == null) {
            throw new IllegalArgumentException("profileId and id are required");
        }

        Treasure existing = treasureRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treasure not found"));

        // Best-effort media cleanup: never let a failed object delete block the row delete.
        if (existing.getMediaObjectKey() != null) {
            try {
                treasureStorageService.deleteObject(existing.getMediaObjectKey());
            } catch (Exception e) {
                log.warn("Failed to delete treasure media object {} for treasureId={}; deleting row anyway",
                        existing.getMediaObjectKey(), id, e);
            }
        }

        treasureRepository.delete(existing);
    }

    private TreasureResponse toResponseWithUrl(Treasure entity) {
        String mediaUrl = entity.getMediaObjectKey() == null
                ? null
                : treasureStorageService.generatePresignedUrl(entity.getMediaObjectKey(), PRESIGNED_URL_VALIDITY_SECONDS);
        return treasureMapper.toResponseDTO(entity, mediaUrl);
    }

    private MediaAttachment.MediaType resolveMediaType(String mimeType) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return MediaAttachment.MediaType.IMAGE;
            }
            if (mimeType.startsWith("audio/")) {
                return MediaAttachment.MediaType.AUDIO;
            }
            if (mimeType.startsWith("video/")) {
                return MediaAttachment.MediaType.VIDEO;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Only image, audio or video media is supported");
    }
}
