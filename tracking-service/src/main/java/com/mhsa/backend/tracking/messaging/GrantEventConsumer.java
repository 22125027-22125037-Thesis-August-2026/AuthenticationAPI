package com.mhsa.backend.tracking.messaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.tracking.config.GrantMessagingConfig;
import com.mhsa.backend.tracking.entity.AccessScope;
import com.mhsa.backend.tracking.entity.GrantStatus;
import com.mhsa.backend.tracking.service.GrantReplicaService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes auth-service grant events and keeps the local {@code data_access_grants} replica in sync.
 *
 * <p>Manual ack with a single consumer (see {@link GrantMessagingConfig}). Both the created and
 * revoked queues carry the same enriched payload; the {@code status} field in the body — not the
 * queue — decides whether access is on or off, so both bind to one handler.
 *
 * <p>Failure handling avoids infinite nack loops:
 * <ul>
 *   <li>Malformed / unparseable / unknown-status messages are dead-lettered immediately.</li>
 *   <li>Transient processing errors are also dead-lettered (requeue=false) and left for the nightly
 *       reconciliation job to heal, rather than requeued forever.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GrantEventConsumer {

    private final ObjectMapper objectMapper;
    private final GrantReplicaService replicaService;

    @RabbitListener(
            queues = GrantMessagingConfig.GRANT_CREATED_QUEUE,
            containerFactory = GrantMessagingConfig.MANUAL_ACK_FACTORY)
    public void onGrantCreated(Message message, Channel channel) throws IOException {
        handle(message, channel);
    }

    @RabbitListener(
            queues = GrantMessagingConfig.GRANT_REVOKED_QUEUE,
            containerFactory = GrantMessagingConfig.MANUAL_ACK_FACTORY)
    public void onGrantRevoked(Message message, Channel channel) throws IOException {
        handle(message, channel);
    }

    private void handle(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        GrantEvent event;
        GrantStatus status;
        AccessScope scope;
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);
            event = GrantEvent.fromJson(node);
            if (!event.isValid()) {
                throw new IllegalArgumentException("missing required fields: " + body);
            }
            status = GrantStatus.valueOf(event.status().trim().toUpperCase(Locale.ROOT));
            // accessScope is optional on the wire; an unknown value is tolerated (applied as null).
            scope = parseScopeOrNull(event.accessScope());
        } catch (Exception parseError) {
            log.error("Malformed grant event, dead-lettering: {}", parseError.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        try {
            replicaService.applyEvent(
                    event.grantId(),
                    event.granterProfileId(),
                    event.granteeProfileId(),
                    status,
                    scope,
                    event.grantedAt(),
                    event.expiresAt(),
                    event.occurredAt());
            channel.basicAck(deliveryTag, false);
        } catch (Exception processingError) {
            // Transient failure (e.g. DB unavailable): dead-letter instead of requeue-looping.
            // The nightly reconciliation job converges the replica back to the snapshot.
            log.error("Failed to apply grant event (grantId={}, granter={}, grantee={}), dead-lettering",
                    event.grantId(), event.granterProfileId(), event.granteeProfileId(), processingError);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private static AccessScope parseScopeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AccessScope.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
