package com.mhsa.backend.tracking.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the data-access-grant read-model stream consumed from auth-service.
 *
 * <p>Declares <b>dedicated, durable</b> queues bound to auth-service's topic exchange on routing
 * keys {@code auth.grant.created} / {@code auth.grant.revoked}, plus a shared dead-letter
 * exchange/queue for poison messages. Dedicated queue names (rather than the shared
 * {@code auth.grant.created} default-exchange queue) guarantee tracking receives <i>every</i> event
 * instead of competing with other consumers, and let us attach DLQ args without clashing with how
 * other services declared the legacy queues.
 *
 * <p>The listener container factory uses <b>manual</b> acknowledgement with a single consumer so the
 * consumer controls ack/dead-letter, same-key events stay serialized (keeping the watermark guard
 * correct under replays), and a failure never falls into an infinite nack-requeue loop.
 *
 * <p>The topic exchange is owned by auth-service; we redeclare it idempotently (matching
 * type/durability) so tracking can boot and bind even if it starts first.
 */
@Configuration
public class GrantMessagingConfig {

    public static final String GRANT_CREATED_ROUTING_KEY = "auth.grant.created";
    public static final String GRANT_REVOKED_ROUTING_KEY = "auth.grant.revoked";

    public static final String GRANT_CREATED_QUEUE = "tracking.auth.grant.created";
    public static final String GRANT_REVOKED_QUEUE = "tracking.auth.grant.revoked";

    public static final String GRANT_DLQ = "tracking.auth.grant.dlq";
    public static final String GRANT_DLX = "tracking.auth.grant.dlx";

    public static final String MANUAL_ACK_FACTORY = "grantManualAckListenerContainerFactory";

    /** Name of auth-service's topic exchange. Must match {@code mhsa.auth.events.exchange} there. */
    @Value("${mhsa.auth.events.exchange:auth.events}")
    private String authEventsExchange;

    @Bean
    public TopicExchange authEventsExchange() {
        return new TopicExchange(authEventsExchange, true, false);
    }

    @Bean
    public Queue grantCreatedQueue() {
        return QueueBuilder.durable(GRANT_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", GRANT_DLX)
                .withArgument("x-dead-letter-routing-key", GRANT_DLQ)
                .build();
    }

    @Bean
    public Queue grantRevokedQueue() {
        return QueueBuilder.durable(GRANT_REVOKED_QUEUE)
                .withArgument("x-dead-letter-exchange", GRANT_DLX)
                .withArgument("x-dead-letter-routing-key", GRANT_DLQ)
                .build();
    }

    @Bean
    public Binding grantCreatedBinding() {
        return BindingBuilder.bind(grantCreatedQueue())
                .to(authEventsExchange())
                .with(GRANT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding grantRevokedBinding() {
        return BindingBuilder.bind(grantRevokedQueue())
                .to(authEventsExchange())
                .with(GRANT_REVOKED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange grantDeadLetterExchange() {
        return new DirectExchange(GRANT_DLX, true, false);
    }

    @Bean
    public Queue grantDeadLetterQueue() {
        return QueueBuilder.durable(GRANT_DLQ).build();
    }

    @Bean
    public Binding grantDeadLetterBinding() {
        return BindingBuilder.bind(grantDeadLetterQueue())
                .to(grantDeadLetterExchange())
                .with(GRANT_DLQ);
    }

    @Bean(MANUAL_ACK_FACTORY)
    public SimpleRabbitListenerContainerFactory grantManualAckListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
