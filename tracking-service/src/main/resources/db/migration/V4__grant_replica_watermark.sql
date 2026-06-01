-- data_access_grants is now maintained as a read-model replica of auth-service's grants:
-- a durable RabbitMQ consumer applies grant.created/grant.revoked events, and a nightly job
-- reconciles against auth-service's /internal/grants snapshot.
--
-- last_event_at is the out-of-order watermark: an event is applied only when its occurredAt is
-- >= the stored watermark, so a replayed/stale message can never overwrite newer state. Plain
-- TIMESTAMP to match the other Instant-backed columns here (ddl-auto=validate); instants are
-- stored/compared in UTC so ordering is unaffected.
ALTER TABLE data_access_grants
    ADD COLUMN IF NOT EXISTS last_event_at TIMESTAMP;
