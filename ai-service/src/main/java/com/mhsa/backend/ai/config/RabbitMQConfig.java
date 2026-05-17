package com.mhsa.backend.ai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue authUserDeletedQueue() {
        return new Queue("auth.user.deleted", true);
    }

    @Bean
    public Queue authUserUpdatedQueue() {
        return new Queue("auth.user.updated", true);
    }

    @Bean
    public Queue authGrantCreatedQueue() {
        return new Queue("auth.grant.created", true);
    }

    @Bean
    public Queue authTokenRevokedQueue() {
        return new Queue("auth.token.revoked", true);
    }
}
