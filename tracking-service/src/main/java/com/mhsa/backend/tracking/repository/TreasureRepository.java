package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.Treasure;

@Repository
public interface TreasureRepository extends JpaRepository<Treasure, UUID> {

    List<Treasure> findByProfileIdOrderByCreatedAtDesc(UUID profileId);

    Optional<Treasure> findByIdAndProfileId(UUID id, UUID profileId);

    void deleteByProfileId(UUID profileId);
}
