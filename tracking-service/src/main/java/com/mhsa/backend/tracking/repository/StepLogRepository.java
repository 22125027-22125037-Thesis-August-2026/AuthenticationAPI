package com.mhsa.backend.tracking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.StepLog;

@Repository
public interface StepLogRepository extends JpaRepository<StepLog, UUID> {

    List<StepLog> findByProfileIdOrderByEntryDateDesc(UUID profileId);

    List<StepLog> findByProfileIdAndEntryDateBetween(UUID profileId, LocalDate from, LocalDate to);

    Optional<StepLog> findByProfileIdAndEntryDate(UUID profileId, LocalDate date);

    Optional<StepLog> findByIdAndProfileId(UUID id, UUID profileId);

    void deleteByProfileId(UUID profileId);
}
