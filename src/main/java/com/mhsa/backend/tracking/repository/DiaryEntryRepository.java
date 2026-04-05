package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.DiaryEntry;

@Repository
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, UUID> {

    List<DiaryEntry> findByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<DiaryEntry> findTopByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<DiaryEntry> findByIdAndProfileId(UUID id, UUID profileId);
}
