package com.mhsa.backend.tracking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.Streak;

@Repository
public interface StreakRepository extends JpaRepository<Streak, UUID> {

    Optional<Streak> findByProfileId(UUID profileId);

    Optional<Streak> findByIdAndProfileId(UUID id, UUID profileId);
}
