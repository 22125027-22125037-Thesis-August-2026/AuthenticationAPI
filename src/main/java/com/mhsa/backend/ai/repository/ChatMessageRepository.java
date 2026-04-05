package com.mhsa.backend.ai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.ai.entity.ChatMessage;
import com.mhsa.backend.ai.entity.ChatSession;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findBySessionOrderBySentAtAsc(ChatSession session);

    @Query("SELECT m FROM ChatMessage m WHERE m.session = :session ORDER BY m.sentAt DESC")
    List<ChatMessage> findTop10BySessionOrderBySentAtDesc(@Param("session") ChatSession session);

    Optional<ChatMessage> findTopBySessionOrderBySentAtDesc(ChatSession session);

    Optional<ChatMessage> findTopBySessionAndSenderOrderBySentAtDesc(ChatSession session, String sender);
}
