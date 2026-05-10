package com.mhsa.backend.tracking.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mhsa.backend.tracking.dto.StreakResponse;
import com.mhsa.backend.tracking.entity.Streak;
import com.mhsa.backend.tracking.mapper.StreakMapper;
import com.mhsa.backend.tracking.repository.StreakRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class StreakServiceImplTest {

    @InjectMocks
    private StreakServiceImpl streakService;

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private StreakMapper streakMapper;

    private UUID profileId;
    private Streak sampleStreak;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        profileId = UUID.randomUUID();
        sampleStreak = Streak.builder()
                .profileId(profileId)
                .streakType("DAILY_TRACKING")
                .currentCount(5)
                .longestCount(8)
                .lastLoggedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void shouldIncrementStreak_whenLastLoggedWasYesterday() {
        // arrange
        sampleStreak.setCurrentCount(5);
        sampleStreak.setLastLoggedAt(LocalDateTime.now().minusDays(1));

        when(streakRepository.findByProfileId(profileId)).thenReturn(Optional.of(sampleStreak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakMapper.toResponseDTO(any(Streak.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        // act
        StreakResponse response = streakService.updateStreak(profileId);

        // assert
        ArgumentCaptor<Streak> captor = ArgumentCaptor.forClass(Streak.class);
        verify(streakRepository).save(any(Streak.class));
        verify(streakRepository).save(captor.capture());

        Streak persistedStreak = captor.getValue();
        assertEquals(6, persistedStreak.getCurrentCount());
        assertNotNull(persistedStreak.getLastLoggedAt());
        assertEquals(6, response.getCurrentCount());
    }

    @Test
    void shouldKeepStreakSame_whenLastLoggedIsToday() {
        // arrange
        sampleStreak.setCurrentCount(5);
        sampleStreak.setLastLoggedAt(LocalDateTime.now());

        when(streakRepository.findByProfileId(profileId)).thenReturn(Optional.of(sampleStreak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakMapper.toResponseDTO(any(Streak.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        // act
        StreakResponse response = streakService.updateStreak(profileId);

        // assert
        ArgumentCaptor<Streak> captor = ArgumentCaptor.forClass(Streak.class);
        verify(streakRepository).save(any(Streak.class));
        verify(streakRepository).save(captor.capture());

        Streak persistedStreak = captor.getValue();
        assertEquals(5, persistedStreak.getCurrentCount());
        assertNotNull(persistedStreak.getLastLoggedAt());
        assertEquals(5, response.getCurrentCount());
    }

    @Test
    void shouldResetStreak_whenLastLoggedWasBeforeYesterday() {
        // arrange
        sampleStreak.setCurrentCount(5);
        sampleStreak.setLastLoggedAt(LocalDateTime.now().minusDays(2));

        when(streakRepository.findByProfileId(profileId)).thenReturn(Optional.of(sampleStreak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakMapper.toResponseDTO(any(Streak.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        // act
        StreakResponse response = streakService.updateStreak(profileId);

        // assert
        ArgumentCaptor<Streak> captor = ArgumentCaptor.forClass(Streak.class);
        verify(streakRepository).save(any(Streak.class));
        verify(streakRepository).save(captor.capture());

        Streak persistedStreak = captor.getValue();
        assertEquals(1, persistedStreak.getCurrentCount());
        assertNotNull(persistedStreak.getLastLoggedAt());
        assertEquals(1, response.getCurrentCount());
    }

    @Test
    void shouldCreateNewStreak_whenUserHasNoStreakYet() {
        // arrange
        when(streakRepository.findByProfileId(profileId)).thenReturn(Optional.empty());
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakMapper.toResponseDTO(any(Streak.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        // act
        StreakResponse response = streakService.updateStreak(profileId);

        // assert
        ArgumentCaptor<Streak> captor = ArgumentCaptor.forClass(Streak.class);
        verify(streakRepository).save(any(Streak.class));
        verify(streakRepository).save(captor.capture());

        Streak persistedStreak = captor.getValue();
        assertEquals(profileId, persistedStreak.getProfileId());
        assertEquals(1, persistedStreak.getCurrentCount());
        assertNotNull(persistedStreak.getLastLoggedAt());
        assertEquals(1, response.getCurrentCount());
    }

    private StreakResponse toResponse(Streak streak) {
        return StreakResponse.builder()
                .streakType(streak.getStreakType())
                .currentCount(streak.getCurrentCount())
                .longestCount(streak.getLongestCount())
                .lastLoggedAt(streak.getLastLoggedAt())
                .build();
    }
}
