-- Opaque, rotating refresh tokens backing the access/refresh token flow.
-- Only the SHA-256 hash of the raw token is stored; the raw value is shown to the
-- client exactly once (at login/refresh) and is never persisted or logged.
--
-- Rotation: each successful /auth/refresh marks the presented row revoked, sets
-- replaced_by to the new row, and issues a fresh token. Presenting an already-revoked
-- token (reuse) is treated as theft and revokes every active token for that user.
--
-- TIMESTAMP (not TIMESTAMPTZ) matches every other Instant-backed column in this schema;
-- with spring.jpa.ddl-auto=validate the entity's Instant fields must agree with the
-- column type. Instants are stored/compared in UTC, so expiry logic is unaffected.
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  UUID         PRIMARY KEY,
    user_id             UUID         NOT NULL,
    token_hash          VARCHAR(64)  NOT NULL,
    device_label        VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL,
    expires_at          TIMESTAMP    NOT NULL,
    absolute_expires_at TIMESTAMP,
    revoked_at          TIMESTAMP,
    replaced_by         UUID,
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

-- Lookups: by hash on refresh (covered by the unique constraint), by user on
-- revoke-all / reuse-detection, and by expiry for the nightly cleanup sweep.
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
