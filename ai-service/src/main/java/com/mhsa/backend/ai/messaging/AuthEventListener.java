package com.mhsa.backend.ai.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListener {

    @RabbitListener(queues = "auth.grant.created")
    public void onGrantCreated(String event) {
        log.info("Grant created event received: {}", event);
    }

    @RabbitListener(queues = "auth.user.updated")
    public void onUserUpdated(String event) {
        log.info("User updated event received: {}", event);
    }

    @RabbitListener(queues = "auth.token.revoked")
    public void onTokenRevoked(String event) {
        log.info("Token revoked event received: {}", event);
    }
}
