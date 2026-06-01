package com.mhsa.backend.auth.messaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.auth.config.AssignmentMessagingConfig;
import com.mhsa.backend.auth.model.AssignmentStatus;
import com.mhsa.backend.auth.service.AssignmentReplicaService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes therapist-api assignment-change events and keeps the local read-model in sync.
 *
 * <p>Manual ack with a single consumer (see {@link AssignmentMessagingConfig}). The body is parsed
 * with the shared {@link ObjectMapper} rather than a global Jackson AMQP converter, so the existing
 * String-based publishing path is left untouched.
 *
 * <p>Failure handling avoids infinite nack loops:
 * <ul>
 *   <li>Malformed / unparseable / unknown-status messages are dead-lettered immediately.</li>
 *   <li>Transient processing errors are also dead-lettered (requeue=false) and left for the
 *       nightly reconciliation job to heal, rather than requeued forever.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TherapistAssignmentConsumer {

    private final ObjectMapper objectMapper;
    private final AssignmentReplicaService replicaService;

    @RabbitListener(
            queues = AssignmentMessagingConfig.ASSIGNMENT_QUEUE,
            containerFactory = AssignmentMessagingConfig.MANUAL_ACK_FACTORY)
    public void onAssignmentChanged(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        AssignmentEvent event;
        AssignmentStatus status;
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            event = objectMapper.readValue(body, AssignmentEvent.class);
            if (event == null || !event.isValid()) {
                throw new IllegalArgumentException("missing required fields: " + body);
            }
            status = AssignmentStatus.valueOf(event.status().trim().toUpperCase(Locale.ROOT));
        } catch (Exception parseError) {
            log.error("Malformed assignment event, dead-lettering: {}", parseError.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        try {
            replicaService.applyEvent(
                    event.therapistProfileId(),
                    event.patientProfileId(),
                    status,
                    event.occurredAt());
            channel.basicAck(deliveryTag, false);
        } catch (Exception processingError) {
            // Transient failure (e.g. DB unavailable): dead-letter instead of requeue-looping.
            // The nightly reconciliation job converges the replica back to the snapshot.
            log.error("Failed to apply assignment event (therapist={}, patient={}), dead-lettering",
                    event.therapistProfileId(), event.patientProfileId(), processingError);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
