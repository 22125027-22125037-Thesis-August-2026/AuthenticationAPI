package com.mhsa.backend.tracking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.entity.DiaryEntry;
import com.mhsa.backend.tracking.entity.MediaAttachment;
import com.mhsa.backend.tracking.mapper.DiaryEntryMapper;
import com.mhsa.backend.tracking.repository.DiaryEntryRepository;
import com.mhsa.backend.tracking.repository.MediaAttachmentRepository;

@ExtendWith(MockitoExtension.class)
class DiaryEntryServiceImplTest {

    @InjectMocks
    private DiaryEntryServiceImpl diaryEntryService;

    @Mock
    private DiaryEntryRepository diaryEntryRepository;

    @Mock
    private MediaAttachmentRepository mediaAttachmentRepository;

    @Mock
    private DiaryEntryMapper diaryEntryMapper;

    @Mock
    private StreakService streakService;

    private UUID profileId;
    private DiaryEntryRequest request;
    private DiaryEntry mappedEntity;
    private DiaryEntry savedEntity;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();

        request = DiaryEntryRequest.builder()
                .content("Today was a productive day, I felt really focused.")
                .moodTag("MOTIVATED")
                .positivityScore(8)
                .build();

        mappedEntity = DiaryEntry.builder()
                .moodTag("MOTIVATED")
                .positivityScore(8)
                .build();

        savedEntity = DiaryEntry.builder()
                .id(UUID.randomUUID())
                .profileId(profileId)
                .moodTag("MOTIVATED")
                .positivityScore(8)
                .entryDate(LocalDate.now())
                .mediaAttachments(new java.util.ArrayList<>())
                .build();
    }

    @Test
    void shouldSaveDiaryAndAttachments_whenAllSucceed() {
        // given
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("entry-photo.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(245760L);

        when(diaryEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(diaryEntryRepository.save(any(DiaryEntry.class))).thenReturn(savedEntity);
        when(mediaAttachmentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        DiaryEntryResponse mappedResponse = DiaryEntryResponse.builder()
                .id(savedEntity.getId())
                .moodTag("MOTIVATED")
                .positivityScore(8)
                .build();
        when(diaryEntryMapper.toResponseDTO(any(DiaryEntry.class))).thenReturn(mappedResponse);

        // when
        DiaryEntryResponse response = diaryEntryService.create(profileId, request, List.of(file));

        // then
        verify(diaryEntryRepository).save(any(DiaryEntry.class));
        verify(mediaAttachmentRepository).saveAll(anyList());

        ArgumentCaptor<List<MediaAttachment>> attachmentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mediaAttachmentRepository).saveAll(attachmentCaptor.capture());
        List<MediaAttachment> persistedAttachments = attachmentCaptor.getValue();

        assertEquals(1, persistedAttachments.size());
        assertEquals("entry-photo.jpg", persistedAttachments.get(0).getFileName());
        assertEquals("image/jpeg", persistedAttachments.get(0).getMimeType());
        assertNotNull(response);
        assertEquals(savedEntity.getId(), response.getId());
    }

    @Test
    void shouldRollback_whenAttachmentProcessingFails() {
        // given
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("entry-photo.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(245760L);

        when(diaryEntryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(diaryEntryRepository.save(any(DiaryEntry.class))).thenReturn(savedEntity);
        when(mediaAttachmentRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("Simulated storage failure"));

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diaryEntryService.create(profileId, request, List.of(file))
        );

        // then
        assertEquals("Simulated storage failure", exception.getMessage());
        verify(diaryEntryRepository).save(any(DiaryEntry.class));
        verify(mediaAttachmentRepository).saveAll(anyList());
        verify(diaryEntryMapper, never()).toResponseDTO(any(DiaryEntry.class));
        verify(streakService, never()).updateStreak(any(UUID.class));
    }
}
