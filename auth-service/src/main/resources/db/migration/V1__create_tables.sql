-- Auth service database schema
-- Tables: users, profiles (and subtypes), data_access_grants

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255),
    dob DATE,
    phone_number VARCHAR(255),
    credits_balance INTEGER,
    pin_code VARCHAR(10),
    account_type VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT users_role_check CHECK (role IN (
        'MANAGER', 'DEPENDENT', 'DOCTOR',
        'PARENT', 'TEEN', 'THERAPIST', 'ADMIN'
    )),
    CONSTRAINT users_account_type_check CHECK (
        account_type IS NULL OR account_type IN ('PARENT', 'CHILD')
    )
);

CREATE TABLE IF NOT EXISTS profiles (
    profile_id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    date_of_birth DATE,
    phone_number VARCHAR(255),
    gender VARCHAR(20),
    profile_type VARCHAR(31) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id)
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

CREATE TABLE IF NOT EXISTS data_access_grants (
    grant_id           UUID         PRIMARY KEY,
    granter_profile_id UUID         NOT NULL,
    grantee_profile_id UUID         NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    access_scope       VARCHAR(20)  NOT NULL,
    granted_at         TIMESTAMP    NOT NULL,
    expires_at         TIMESTAMP,
    CONSTRAINT fk_dag_granter FOREIGN KEY (granter_profile_id) REFERENCES profiles(profile_id),
    CONSTRAINT fk_dag_grantee FOREIGN KEY (grantee_profile_id) REFERENCES profiles(profile_id),
    CONSTRAINT chk_dag_status CHECK (status IN ('ACTIVE', 'REVOKED')),
    CONSTRAINT chk_dag_scope  CHECK (access_scope IN ('READ_JOURNAL', 'READ_ALL'))
);

CREATE INDEX IF NOT EXISTS idx_data_access_grants_lookup
    ON data_access_grants (granter_profile_id, grantee_profile_id, status);
