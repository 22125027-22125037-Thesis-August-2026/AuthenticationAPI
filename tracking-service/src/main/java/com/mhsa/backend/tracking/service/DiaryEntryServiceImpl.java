package com.mhsa.backend.tracking.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
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
                    .map(file -> {
                        String fileName = file.getOriginalFilename();
                        String fileType = file.getContentType();
                        log.debug("Preparing attachment metadata: profileId={}, fileName={}, mimeType={}", profileId,
                                fileName, fileType);

                        // TODO: Implement actual file storage (e.g., local disk or S3) and generate fileUrl.
                        String generatedFileUrl = String.format("/files/diary/%s/%s", savedEntity.getId(), fileName);

                        return MediaAttachment.builder()
                                .profileId(profileId)
                                .diaryEntry(savedEntity)
                                .fileName(fileName)
                                .mimeType(fileType)
                                .fileUrl(generatedFileUrl)
                                .fileSizeBytes(file.getSize())
                                .mediaType(resolveMediaType(fileType))
                                .build();
                    })
                    .toList();

            List<MediaAttachment> savedAttachments = mediaAttachmentRepository.saveAll(attachmentsToSave);
            savedEntity.getMediaAttachments().addAll(savedAttachments);
            log.info("Saved {} attachment record(s) for diaryEntryId={}", savedAttachments.size(), savedEntity.getId());
        }

        // 3) Update streak and return response DTO.
        streakService.updateStreak(profileId);
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
            existing.getMediaAttachments().clear();

            List<MediaAttachment> attachmentsToSave = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        String fileName = file.getOriginalFilename();
                        String fileType = file.getContentType();
                        String generatedFileUrl = String.format("/files/diary/%s/%s", existing.getId(), fileName);

                        return MediaAttachment.builder()
                                .profileId(profileId)
                                .diaryEntry(existing)
                                .fileName(fileName)
                                .mimeType(fileType)
                                .fileUrl(generatedFileUrl)
                                .fileSizeBytes(file.getSize())
                                .mediaType(resolveMediaType(fileType))
                                .build();
                    })
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
        diaryEntryRepository.delete(existing);
    }

    private DiaryEntry findOwnedDiaryEntry(UUID profileId, UUID id) {
        return diaryEntryRepository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary entry not found"));
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
