-- Daily step tracking
-- One row per profile per day (upsert), modeled on food_logs.

CREATE TABLE step_logs (
    step_log_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    step_count INTEGER NOT NULL DEFAULT 0,
    goal INTEGER NOT NULL DEFAULT 6000,
    source VARCHAR(50) NOT NULL DEFAULT 'DEVICE_SENSOR',
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    logged_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_step_logs_profile_entry_date UNIQUE(profile_id, entry_date)
);

CREATE INDEX idx_step_logs_profile_entry_date ON step_logs(profile_id, entry_date);
