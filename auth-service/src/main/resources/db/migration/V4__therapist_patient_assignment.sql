-- Local read-model replica of therapist<->patient assignments owned by therapist-api.
-- Kept in sync by a durable RabbitMQ consumer (event stream) and healed nightly by a
-- reconciliation job that pages through therapist-api's /internal/assignments snapshot.
-- Used by AccessGuard.canViewPatientProfile to authorize therapist reads of patient profiles.

-- NOTE: the spec calls these TIMESTAMPTZ; we use TIMESTAMP to match every other Instant-backed
-- column in this schema (granted_at, expires_at, created_at, ...). With spring.jpa.ddl-auto=validate
-- the entity's Instant fields must agree with the column type, and TIMESTAMP is the proven-working
-- mapping here. Instants are stored/compared in UTC, so the watermark/ordering logic is unaffected.
CREATE TABLE IF NOT EXISTS therapist_patient_assignment (
    therapist_profile_id UUID        NOT NULL,
    patient_profile_id   UUID        NOT NULL,
    status               VARCHAR(20) NOT NULL,
    assigned_at          TIMESTAMP,
    last_event_at        TIMESTAMP,
    updated_at           TIMESTAMP,
    CONSTRAINT pk_therapist_patient_assignment
        PRIMARY KEY (therapist_profile_id, patient_profile_id),
    CONSTRAINT chk_tpa_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Authorization lookups filter by patient_profile_id (+ caller's therapist_profile_id, which
-- is covered by the PK's leading column when the therapist is known); index the patient side
-- so reverse lookups stay cheap.
CREATE INDEX IF NOT EXISTS idx_tpa_patient_profile_id
    ON therapist_patient_assignment (patient_profile_id);
