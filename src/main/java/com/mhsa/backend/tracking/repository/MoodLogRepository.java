package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.MoodLog;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, UUID> {

    // Uses explicit query to keep API name aligned with business wording (logDate).
    @Query("SELECT m FROM MoodLog m WHERE m.profileId = :profileId ORDER BY m.loggedAt DESC")
    List<MoodLog> findByProfileIdOrderByLogDateDesc(@Param("profileId") UUID profileId);
}
