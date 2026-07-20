package com.mhsa.backend.auth.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.auth.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${mhsa.auth.events.exchange:auth.events}")
    private String authEventsExchange;

    /**
     * Publishes the enriched grant event to the topic exchange, but only after the surrounding grant
     * transaction has committed. A rolled-back grant therefore never emits an event. ACTIVE grants
     * route on {@code auth.grant.created}; any grant that has left ACTIVE (revoked/expired) routes on
     * {@code auth.grant.revoked} so downstream replicas can drop access.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGrantChanged(GrantChangedEvent event) {
        String routingKey = event.isActive()
                ? RabbitMQConfig.GRANT_CREATED_ROUTING_KEY
                : RabbitMQConfig.GRANT_REVOKED_ROUTING_KEY;
        String payload = toJson(event);
        rabbitTemplate.convertAndSend(authEventsExchange, routingKey, payload);
        log.info("Published grant event [{}]: grantId={}, granter={}, grantee={}, status={}",
                routingKey, event.grantId(), event.granterProfileId(), event.granteeProfileId(),
                event.status());
    }

    /**
     * Publishes a therapist profile/avatar/license change to the topic exchange, but only after the
     * editing transaction commits — a rolled-back edit therefore never emits. The payload is the flat
     * profile snapshot ({@link TherapistProfileView}) plus the {@code eventId}/{@code occurredAt}
     * envelope fields, all at top level. therapist-api binds its own queue to this exchange on
     * routing key {@code therapist.profile.updated}.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTherapistProfileChanged(TherapistProfileChangedEvent event) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("eventId", event.eventId() == null ? null : event.eventId().toString());
            payload.put("occurredAt", event.occurredAt() == null ? null : event.occurredAt().toString());
            // Merge the flat profile fields (profileId, fullName, ... licenseStatus) at top level.
            payload.setAll(event.profile().toJson(objectMapper));

            rabbitTemplate.convertAndSend(
                    authEventsExchange,
                    RabbitMQConfig.THERAPIST_PROFILE_UPDATED_ROUTING_KEY,
                    objectMapper.writeValueAsString(payload));
            log.info("Published therapist.profile.updated event: profileId={}, eventId={}",
                    event.profileId(), event.eventId());
        } catch (Exception e) {
            // Runs after commit, so we cannot fail the transaction; log and let nightly
            // reconciliation on therapist-api's side heal any miss.
            log.error("Failed to publish therapist.profile.updated event for profileId={}",
                    event.profileId(), e);
        }
    }

    /**
     * Serializes the event to a flat JSON object. Built by hand (rather than via a shared
     * ObjectMapper) so {@code Instant} values are emitted as ISO-8601 strings without depending on
     * any Jackson date-module configuration. All values here (UUIDs, enum names, ISO instants) are
     * JSON-safe and need no escaping.
     */
    private static String toJson(GrantChangedEvent e) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        appendString(sb, "eventId", e.eventId() == null ? null : e.eventId().toString());
        sb.append(',');
        appendString(sb, "occurredAt", e.occurredAt() == null ? null : e.occurredAt().toString());
        sb.append(',');
        appendString(sb, "grantId", e.grantId() == null ? null : e.grantId().toString());
        sb.append(',');
        appendString(sb, "granterProfileId",
                e.granterProfileId() == null ? null : e.granterProfileId().toString());
        sb.append(',');
        appendString(sb, "granteeProfileId",
                e.granteeProfileId() == null ? null : e.granteeProfileId().toString());
        sb.append(',');
        appendString(sb, "status", e.status() == null ? null : e.status().name());
        sb.append(',');
        appendString(sb, "accessScope", e.accessScope());
        sb.append(',');
        appendString(sb, "grantedAt", e.grantedAt() == null ? null : e.grantedAt().toString());
        sb.append(',');
        appendString(sb, "expiresAt", e.expiresAt() == null ? null : e.expiresAt().toString());
        sb.append('}');
        return sb.toString();
    }

    private static void appendString(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"').append(value).append('"');
        }
    }
}
