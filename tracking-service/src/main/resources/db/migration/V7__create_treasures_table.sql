-- Treasure Box ("Hộp Trân Quý") — a personal comfort/safe-space collection.
-- Each treasure is a short note in one of 8 categories, optionally anchored by a
-- single inline media item (image/audio/video) stored in object storage (S3/MinIO).
-- Only the object key is persisted; responses carry a freshly signed GET URL.

CREATE TABLE treasures (
    treasure_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    category VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    emoji VARCHAR(32),
    media_object_key VARCHAR(1000),
    media_type VARCHAR(20),
    mime_type VARCHAR(100),
    created_at TIMESTAMP,
    CONSTRAINT chk_treasures_media_type CHECK (media_type IN ('IMAGE', 'AUDIO', 'VIDEO'))
);

CREATE INDEX idx_treasures_profile_created_at ON treasures(profile_id, created_at DESC);
