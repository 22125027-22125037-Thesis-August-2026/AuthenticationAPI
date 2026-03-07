package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.FoodLog;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, UUID> {

    List<FoodLog> findByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<FoodLog> findByIdAndProfileId(UUID id, UUID profileId);
}
