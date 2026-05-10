package com.mhsa.backend.tracking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.FoodLog;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, UUID> {

    List<FoodLog> findByProfileIdOrderByEntryDateDesc(UUID profileId);

    List<FoodLog> findByProfileIdAndEntryDateBetweenOrderByEntryDateAsc(UUID profileId, LocalDate startDate, LocalDate endDate);

    Optional<FoodLog> findTopByProfileIdOrderByEntryDateDesc(UUID profileId);

    Optional<FoodLog> findByIdAndProfileId(UUID id, UUID profileId);

    Optional<FoodLog> findByProfileIdAndEntryDate(UUID profileId, LocalDate entryDate);
}
