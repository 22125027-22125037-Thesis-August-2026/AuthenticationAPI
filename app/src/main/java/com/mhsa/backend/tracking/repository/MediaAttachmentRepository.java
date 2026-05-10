package com.mhsa.backend.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.MediaAttachment;

@Repository
public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, UUID> {

    List<MediaAttachment> findByProfileIdOrderByCreatedAtDesc(UUID profileId);

    // referenceType supports DIARY_ENTRY and FOOD_LOG for polymorphic lookups.
    @Query(value = """
            SELECT *
            FROM media_attachments ma
            WHERE (:referenceType = 'DIARY_ENTRY' AND ma.diary_entry_id = :referenceId)
               OR (:referenceType = 'FOOD_LOG' AND ma.food_log_id = :referenceId)
            """, nativeQuery = true)
    List<MediaAttachment> findByReferenceIdAndReferenceType(
            @Param("referenceId") UUID referenceId,
            @Param("referenceType") String referenceType
    );

        Optional<MediaAttachment> findByIdAndProfileId(UUID id, UUID profileId);
}
