package com.mhsa.backend.auth.config;

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
 * Wiring for the therapist&lt;-&gt;patient assignment read-model stream.
 *
 * <p>Declares a <b>durable</b> queue bound to therapist-api's topic exchange on routing key
 * {@code therapist.assignment.changed}, plus a dead-letter exchange/queue for poison messages.
 * The dedicated listener container factory uses <b>manual</b> acknowledgement so the consumer
 * controls ack/dead-letter and never falls into an infinite nack-requeue loop.
 *
 * <p>The topic exchange is owned by therapist-api; we redeclare it (idempotently, with matching
 * type/durability) so this service can boot and bind even if it starts first.
 */
@Configuration
public class AssignmentMessagingConfig {

    /** Routing key therapist-api publishes assignment changes on. */
    public static final String ASSIGNMENT_CHANGED_ROUTING_KEY = "therapist.assignment.changed";

    /** Local durable queue holding the assignment-change stream for this service. */
    public static final String ASSIGNMENT_QUEUE = "auth.therapist.assignment.changed";

    /** Dead-letter queue for malformed / repeatedly-failing assignment messages. */
    public static final String ASSIGNMENT_DLQ = "auth.therapist.assignment.changed.dlq";

    /** Direct dead-letter exchange fronting {@link #ASSIGNMENT_DLQ}. */
    public static final String ASSIGNMENT_DLX = "auth.therapist.assignment.changed.dlx";

    /** Bean name of the manual-ack listener container factory used by the consumer. */
    public static final String MANUAL_ACK_FACTORY = "assignmentManualAckListenerContainerFactory";

    /** Name of therapist-api's topic exchange. Configurable; defaults to {@code therapist.events}. */
    @Value("${therapist.api.exchange:therapist.events}")
    private String therapistExchange;

    @Bean
    public TopicExchange therapistEventsExchange() {
        // durable, non-auto-delete to match therapist-api's declaration.
        return new TopicExchange(therapistExchange, true, false);
    }

    @Bean
    public Queue assignmentChangedQueue() {
        return QueueBuilder.durable(ASSIGNMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", ASSIGNMENT_DLX)
                .withArgument("x-dead-letter-routing-key", ASSIGNMENT_DLQ)
                .build();
    }

    @Bean
    public Binding assignmentChangedBinding() {
        // Reference bean methods directly (proxyBeanMethods=true returns the managed singletons)
        // so we don't depend on parameter-name disambiguation between the two Queue beans.
        return BindingBuilder.bind(assignmentChangedQueue())
                .to(therapistEventsExchange())
                .with(ASSIGNMENT_CHANGED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange assignmentDeadLetterExchange() {
        return new DirectExchange(ASSIGNMENT_DLX, true, false);
    }

    @Bean
    public Queue assignmentDeadLetterQueue() {
        return QueueBuilder.durable(ASSIGNMENT_DLQ).build();
    }

    @Bean
    public Binding assignmentDeadLetterBinding() {
        return BindingBuilder.bind(assignmentDeadLetterQueue())
                .to(assignmentDeadLetterExchange())
                .with(ASSIGNMENT_DLQ);
    }

    /**
     * Dedicated factory with MANUAL ack and a single consumer. Single concurrency keeps
     * same-key events serialized so the {@code last_event_at} watermark guard stays correct
     * under replays. Scoped to this listener so the existing (auto-ack) publishing path and
     * any future listeners are unaffected.
     */
    @Bean(MANUAL_ACK_FACTORY)
    public SimpleRabbitListenerContainerFactory assignmentManualAckListenerContainerFactory(
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
