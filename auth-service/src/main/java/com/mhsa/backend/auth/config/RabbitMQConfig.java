package com.mhsa.backend.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /** Routing key for grant created/updated events. */
    public static final String GRANT_CREATED_ROUTING_KEY = "auth.grant.created";

    /** Routing key for grant revoked/expired events. */
    public static final String GRANT_REVOKED_ROUTING_KEY = "auth.grant.revoked";

    /** Routing key for therapist profile/avatar/license changes mirrored by therapist-api. */
    public static final String THERAPIST_PROFILE_UPDATED_ROUTING_KEY = "therapist.profile.updated";

    /**
     * Topic exchange that fans out enriched grant events. Defaults to {@code auth.events}; the same
     * name must be configured on consumers (tracking-service) so their queues bind here.
     */
    @Value("${mhsa.auth.events.exchange:auth.events}")
    private String authEventsExchange;

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
    public Queue authGrantRevokedQueue() {
        return new Queue("auth.grant.revoked", true);
    }

    /** Durable, non-auto-delete topic exchange owned by auth-service for its domain events. */
    @Bean
    public TopicExchange authEventsExchange() {
        return new TopicExchange(authEventsExchange, true, false);
    }

    // Bind the legacy default-exchange queues to the topic exchange too, so existing consumers that
    // listen on these queue names (e.g. ai-service on auth.grant.created) keep receiving the now
    // enriched events without any change on their side.
    @Bean
    public Binding authGrantCreatedBinding() {
        return BindingBuilder.bind(authGrantCreatedQueue())
                .to(authEventsExchange())
                .with(GRANT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding authGrantRevokedBinding() {
        return BindingBuilder.bind(authGrantRevokedQueue())
                .to(authEventsExchange())
                .with(GRANT_REVOKED_ROUTING_KEY);
    }
}
