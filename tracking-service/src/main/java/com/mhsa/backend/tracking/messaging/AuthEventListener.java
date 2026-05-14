package com.mhsa.backend.tracking.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListener {

    @RabbitListener(queues = "auth.user.deleted")
    public void onUserDeleted(String event) {
        try {
            log.info("User deleted event received: {}", event);
            // TODO: Parse event JSON to extract userId
            // TODO: Delete all diary_entries, food_logs, mood_logs, sleep_logs, streaks, media_attachments for this user
            // TODO: Invalidate cache entries for this user
        } catch (Exception e) {
            log.error("Error processing user deleted event: {}", event, e);
        }
    }

    @RabbitListener(queues = "auth.user.updated")
    public void onUserUpdated(String event) {
        try {
            log.info("User updated event received: {}", event);
            // TODO: Parse event JSON to extract userId
            // TODO: Invalidate all cached context for this user
            // TODO: Update user metadata if needed
        } catch (Exception e) {
            log.error("Error processing user updated event: {}", event, e);
        }
    }

    @RabbitListener(queues = "auth.grant.created")
    public void onGrantCreated(String event) {
        try {
            log.info("Auth grant created event received: {}", event);
            // TODO: Parse event JSON to extract grantor_id and grantee_id
            // TODO: Determine grant type (READ, WRITE)
            // TODO: Store data access grant in database (Phase 4 feature)
            // TODO: Invalidate cache for affected users
        } catch (Exception e) {
            log.error("Error processing grant created event: {}", event, e);
        }
    }
}
