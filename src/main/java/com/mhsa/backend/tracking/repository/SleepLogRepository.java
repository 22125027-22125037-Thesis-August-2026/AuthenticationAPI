package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.SleepLog;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLog, UUID> {

    List<SleepLog> findByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<SleepLog> findTopByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<SleepLog> findByIdAndProfileId(UUID id, UUID profileId);
}
