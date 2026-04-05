package com.mhsa.backend.ai.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.ai.entity.ChatSession;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    Optional<ChatSession> findByProfileId(UUID profileId);

    long countByProfileId(UUID profileId);

    long countByProfileIdAndCreatedAtBetween(UUID profileId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT s
            FROM ChatSession s
            WHERE s.profileId = :profileId
            ORDER BY COALESCE(
                (SELECT MAX(m.sentAt) FROM ChatMessage m WHERE m.session = s),
                s.createdAt
            ) DESC
            """)
    List<ChatSession> findAllByProfileIdOrderByUpdatedAtDesc(@Param("profileId") UUID profileId);
}
