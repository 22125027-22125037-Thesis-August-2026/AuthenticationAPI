-- Tracking Service Schema
-- All tables migrated from monolith

CREATE TABLE IF NOT EXISTS diary_entries (
    diary_entry_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    title VARCHAR(255),
    content TEXT,
    mood_tag VARCHAR(100),
    positivity_score INTEGER,
    entry_date DATE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_logs (
    food_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    water_glasses INTEGER NOT NULL DEFAULT 0,
    food_description TEXT NOT NULL,
    satiety_level VARCHAR(100) NOT NULL,
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_food_logs_profile_entry_date ON food_logs(profile_id, entry_date);

CREATE TABLE IF NOT EXISTS mood_logs (
    mood_log_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    mood_score INTEGER,
    note TEXT,
    logged_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sleep_logs (
    sleep_log_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    sleep_start_at TIMESTAMP,
    sleep_end_at TIMESTAMP,
    duration_minutes INTEGER,
    sleep_quality INTEGER,
    note TEXT,
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    logged_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sleep_logs_profile_entry_date ON sleep_logs(profile_id, entry_date);

CREATE TABLE IF NOT EXISTS streaks (
    streak_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    streak_type VARCHAR(50) NOT NULL,
    current_count INTEGER NOT NULL DEFAULT 0,
    longest_count INTEGER NOT NULL DEFAULT 0,
    last_logged_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(profile_id, streak_type)
);
CREATE INDEX IF NOT EXISTS idx_streaks_profile_type ON streaks(profile_id, streak_type);

CREATE TABLE IF NOT EXISTS media_attachments (
    media_attachment_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    diary_entry_id UUID,
    food_log_id UUID,
    file_url VARCHAR(1000) NOT NULL,
    file_name VARCHAR(255),
    media_type VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    file_size_bytes BIGINT,
    created_at TIMESTAMP,
    CONSTRAINT fk_media_attachments_diary_entry FOREIGN KEY (diary_entry_id) REFERENCES diary_entries(diary_entry_id)
);
