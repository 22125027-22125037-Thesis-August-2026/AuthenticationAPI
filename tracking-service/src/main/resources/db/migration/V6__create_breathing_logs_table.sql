-- Daily breathing tracking
-- One row per profile per day (upsert), modeled on step_logs.
-- Unlike steps, a session ADDS its duration to the day's total and increments the session count.

CREATE TABLE breathing_logs (
    breathing_log_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    total_duration_seconds INTEGER NOT NULL DEFAULT 0,
    sessions_completed INTEGER NOT NULL DEFAULT 0,
    goal_seconds INTEGER NOT NULL DEFAULT 300,
    source VARCHAR(50) NOT NULL DEFAULT 'GUIDED_SESSION',
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    logged_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_breathing_logs_profile_entry_date UNIQUE(profile_id, entry_date)
);

CREATE INDEX idx_breathing_logs_profile_entry_date ON breathing_logs(profile_id, entry_date);
