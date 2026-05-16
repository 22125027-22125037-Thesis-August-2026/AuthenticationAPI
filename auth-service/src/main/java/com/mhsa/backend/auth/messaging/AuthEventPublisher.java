package com.mhsa.backend.auth.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserDeleted(UUID userId) {
        String event = String.format("{\"userId\":\"%s\"}", userId);
        rabbitTemplate.convertAndSend("auth.user.deleted", event);
        log.info("Published user.deleted event: userId={}", userId);
    }

    public void publishUserUpdated(UUID userId) {
        String event = String.format("{\"userId\":\"%s\"}", userId);
        rabbitTemplate.convertAndSend("auth.user.updated", event);
        log.info("Published user.updated event: userId={}", userId);
    }

    public void publishGrantCreated(UUID granterProfileId, UUID granteeProfileId) {
        String event = String.format("{\"granterProfileId\":\"%s\",\"granteeProfileId\":\"%s\"}",
                granterProfileId, granteeProfileId);
        rabbitTemplate.convertAndSend("auth.grant.created", event);
        log.info("Published grant.created event: granter={}, grantee={}", granterProfileId, granteeProfileId);
    }

    public void publishGrantRevoked(UUID granterProfileId, UUID granteeProfileId) {
        String event = String.format("{\"granterProfileId\":\"%s\",\"granteeProfileId\":\"%s\"}",
                granterProfileId, granteeProfileId);
        rabbitTemplate.convertAndSend("auth.grant.revoked", event);
        log.info("Published grant.revoked event: granter={}, grantee={}", granterProfileId, granteeProfileId);
    }
}
