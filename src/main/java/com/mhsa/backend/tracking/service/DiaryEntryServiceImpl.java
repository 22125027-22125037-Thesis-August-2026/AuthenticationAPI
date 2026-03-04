package com.mhsa.backend.tracking.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.mapper.DiaryEntryMapper;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.MediaAttachmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryEntryServiceImpl implements DiaryEntryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final DiaryEntryMapper diaryEntryMapper;
    private final StreakService streakService;

    @Override
    @Transactional
    public DiaryEntryResponse create(UUID profileId, DiaryEntryRequest request, List<MultipartFile> files) {
        if (request == null || profileId == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        // 1) Save diary metadata/content first.
        DiaryEntry entityToSave = diaryEntryMapper.toEntity(request);
        entityToSave.setProfileId(profileId);
        entityToSave.setEntryDate(LocalDate.now());
        entityToSave.setContent(encrypt(request.getContent()));

        DiaryEntry savedEntity = diaryEntryRepository.save(entityToSave);

        // 2) Save linked attachment records in the same transaction.
        if (files != null && !files.isEmpty()) {
            List<MediaAttachment> attachmentsToSave = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        String fileName = file.getOriginalFilename();
                        String fileType = file.getContentType();

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
        }

        // 3) Update streak and return response DTO.
        streakService.updateStreak(profileId);

        DiaryEntryResponse response = diaryEntryMapper.toResponseDTO(savedEntity);
        response.setContent(decrypt(savedEntity.getContent()));
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
