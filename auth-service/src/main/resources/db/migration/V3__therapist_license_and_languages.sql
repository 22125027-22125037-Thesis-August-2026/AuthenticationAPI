-- Adds license-verification lifecycle fields and a languages column to therapist_profile.
-- Backfills license_status from the legacy is_verified flag so existing rows stay valid.

ALTER TABLE therapist_profile
    ADD COLUMN IF NOT EXISTS license_number       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS license_authority    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS license_expires_at   DATE,
    ADD COLUMN IF NOT EXISTS license_document_url VARCHAR(1024),
    ADD COLUMN IF NOT EXISTS license_status       VARCHAR(40),
    ADD COLUMN IF NOT EXISTS languages            VARCHAR(512);

-- Derive the initial verification state from the existing boolean.
UPDATE therapist_profile
    SET license_status = CASE WHEN is_verified THEN 'VERIFIED' ELSE 'PENDING_VERIFICATION' END
    WHERE license_status IS NULL;

ALTER TABLE therapist_profile
    ALTER COLUMN license_status SET DEFAULT 'PENDING_VERIFICATION';

ALTER TABLE therapist_profile
    ALTER COLUMN license_status SET NOT NULL;

ALTER TABLE therapist_profile
    ADD CONSTRAINT chk_therapist_license_status CHECK (
        license_status IN ('PENDING_VERIFICATION', 'VERIFIED', 'REJECTED', 'EXPIRED')
    );
