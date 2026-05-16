package com.mhsa.backend.ai.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.ai.repository.ChatSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final ChatSessionRepository chatSessionRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/{profileId}/chat-stats")
    public ResponseEntity<ObjectNode> getChatStats(@PathVariable UUID profileId) {
        try {
            ObjectNode stats = objectMapper.createObjectNode();

            var sessions = chatSessionRepository.findAllByProfileIdOrderByUpdatedAtDesc(profileId);
            stats.put("totalSessions", sessions.size());
            stats.put("activeSessions", sessions.size());

            if (!sessions.isEmpty()) {
                var latestSession = sessions.get(0);
                stats.put("latestSessionId", latestSession.getId().toString());
                stats.put("latestSessionDate", latestSession.getCreatedAt().toString());
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting chat stats: profileId={}", profileId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
