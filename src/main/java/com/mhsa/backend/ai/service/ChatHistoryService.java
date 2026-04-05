package com.mhsa.backend.ai.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mhsa.backend.ai.dto.ChatMessageDto;
import com.mhsa.backend.ai.entity.ChatMessage;
import com.mhsa.backend.ai.entity.ChatSession;
import com.mhsa.backend.ai.repository.ChatMessageRepository;
import com.mhsa.backend.ai.repository.ChatSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessageDto> getChatHistory(UUID profileId, UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getProfileId().equals(profileId)) {
            throw new IllegalArgumentException("Session does not belong to the current user");
        }

        return chatMessageRepository.findBySessionOrderBySentAtAsc(session).stream()
                .map(this::toDto)
                .toList();
    }

    private ChatMessageDto toDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .messageId(message.getId())
                .sessionId(message.getSession().getId())
                .sender(message.getSender())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}
