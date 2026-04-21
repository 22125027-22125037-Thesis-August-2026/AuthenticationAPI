-- Create base tables from current JPA entities before seeding data.

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255),
    dob DATE,
    phone_number VARCHAR(255),
    credits_balance INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT users_role_check CHECK (role IN (
        'MANAGER', 'DEPENDENT', 'DOCTOR',
        'PARENT', 'TEEN', 'THERAPIST', 'ADMIN'
    ))
);

CREATE TABLE IF NOT EXISTS profiles (
    profile_id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    date_of_birth DATE,
    phone_number VARCHAR(255),
    profile_type VARCHAR(31) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS parent_profile (
    profile_id UUID PRIMARY KEY,
    linked_teen_id UUID,
    CONSTRAINT fk_parent_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id)
);

CREATE TABLE IF NOT EXISTS teen_profile (
    profile_id UUID PRIMARY KEY,
    school VARCHAR(255),
    emergency_contact VARCHAR(255),
    CONSTRAINT fk_teen_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id)
);

CREATE TABLE IF NOT EXISTS therapist_profile (
    profile_id UUID PRIMARY KEY,
    specialization VARCHAR(255),
    bio TEXT,
    years_of_experience INTEGER,
    consultation_fee NUMERIC(12, 2),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_therapist_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id)
);

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

CREATE INDEX IF NOT EXISTS idx_food_logs_profile_entry_date
    ON food_logs (profile_id, entry_date);

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

CREATE INDEX IF NOT EXISTS idx_sleep_logs_profile_entry_date
    ON sleep_logs (profile_id, entry_date);

CREATE TABLE IF NOT EXISTS streaks (
    streak_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    streak_type VARCHAR(50) NOT NULL,
    current_count INTEGER NOT NULL DEFAULT 0,
    longest_count INTEGER NOT NULL DEFAULT 0,
    last_logged_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_streak_profile_type UNIQUE (profile_id, streak_type)
);

CREATE TABLE IF NOT EXISTS chat_sessions (
    session_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    message_id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    sender VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id)
);

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
