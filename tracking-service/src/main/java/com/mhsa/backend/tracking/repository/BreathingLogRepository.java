package com.mhsa.backend.tracking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.BreathingLog;

@Repository
public interface BreathingLogRepository extends JpaRepository<BreathingLog, UUID> {

    List<BreathingLog> findByProfileIdOrderByEntryDateDesc(UUID profileId);

    List<BreathingLog> findByProfileIdAndEntryDateBetween(UUID profileId, LocalDate from, LocalDate to);

    Optional<BreathingLog> findByProfileIdAndEntryDate(UUID profileId, LocalDate date);

    Optional<BreathingLog> findByIdAndProfileId(UUID id, UUID profileId);

    void deleteByProfileId(UUID profileId);
}
