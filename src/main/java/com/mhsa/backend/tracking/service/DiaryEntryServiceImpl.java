package com.mhsa.backend.tracking.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.mapper.DiaryEntryMapper;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryEntryServiceImpl implements DiaryEntryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final DiaryEntryMapper diaryEntryMapper;
    private final StreakService streakService;

    @Override
    @Transactional
    public DiaryEntryResponse create(DiaryEntryRequest request) {
        if (request == null || request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required");
        }

        DiaryEntry entityToSave = diaryEntryMapper.toEntity(request);
        entityToSave.setContent(encrypt(request.getContent()));

        DiaryEntry savedEntity = diaryEntryRepository.save(entityToSave);
        streakService.updateStreak(request.getProfileId());

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
}
