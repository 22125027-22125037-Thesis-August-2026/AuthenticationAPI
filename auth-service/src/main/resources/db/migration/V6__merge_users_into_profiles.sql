-- Merge the users table into profiles.
--
-- users<->profiles has always been 1:1 (profiles.user_id NOT NULL UNIQUE), every other
-- table in this schema and every other service keys on profile_id, and the JWT already
-- carries profileId. Collapsing the pair makes profile_id the single identity, removes
-- the duplicated demographics (full_name/dob/phone_number lived in both tables), and
-- lets /auth/me return the id the rest of the platform actually uses.
--
-- Role values are normalized to the four the Role enum knows (legacy MANAGER/DEPENDENT/
-- DOCTOR aliases map to PARENT/TEEN/THERAPIST, matching RoleAttributeConverter).

-- 1. Defensive healing: give any profile-less user a profile so no account is lost.
--    Live data has zero such rows (verified 2026-07-16); registration and login both
--    guarantee a profile, so this is belt-and-braces for stray manual inserts.
INSERT INTO profiles (profile_id, user_id, full_name, date_of_birth, phone_number, profile_type, created_at, updated_at)
SELECT
    gen_random_uuid(),
    u.user_id,
    COALESCE(u.full_name, u.email, 'Unnamed User'),
    u.dob,
    u.phone_number,
    CASE
        WHEN u.role IN ('TEEN', 'DEPENDENT')    THEN 'TEEN'
        WHEN u.role IN ('THERAPIST', 'DOCTOR')  THEN 'THERAPIST'
        ELSE 'Profile'
    END,
    now(),
    now()
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.user_id = u.user_id);

-- Normalize legacy discriminator values. V2 seeded base profiles with profile_type
-- 'PARENT', but the base Profile entity's discriminator is Hibernate's default (the
-- entity name, 'Profile'), so those rows have been unloadable by Hibernate since they
-- were seeded ("Unknown discriminator value: PARENT"). Pre-merge this was masked —
-- login read the users table and only /auth/me touched profiles; post-merge login
-- itself loads the profile, so the data must be fixed. TEEN/THERAPIST match their
-- subclasses' @DiscriminatorValue and stay as-is.
UPDATE profiles SET profile_type = 'Profile'
WHERE profile_type NOT IN ('TEEN', 'THERAPIST', 'Profile');

-- Joined-inheritance subtype rows must exist for TEEN/THERAPIST discriminators.
INSERT INTO teen_profile (profile_id)
SELECT p.profile_id FROM profiles p
WHERE p.profile_type = 'TEEN'
  AND NOT EXISTS (SELECT 1 FROM teen_profile t WHERE t.profile_id = p.profile_id);

INSERT INTO therapist_profile (profile_id, is_verified)
SELECT p.profile_id, FALSE FROM profiles p
WHERE p.profile_type = 'THERAPIST'
  AND NOT EXISTS (SELECT 1 FROM therapist_profile t WHERE t.profile_id = p.profile_id);

-- 2. Add the auth columns to profiles.
ALTER TABLE profiles
    ADD COLUMN email           VARCHAR(255),
    ADD COLUMN password        VARCHAR(255),
    ADD COLUMN role            VARCHAR(50),
    ADD COLUMN credits_balance INTEGER,
    ADD COLUMN pin_code        VARCHAR(10),
    ADD COLUMN account_type    VARCHAR(20),
    ADD COLUMN is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN last_login      TIMESTAMP;

-- 3. Copy the account data across, filling demographic gaps from the user row.
UPDATE profiles p
SET email           = u.email,
    password        = u.password,
    role            = CASE
                          WHEN u.role IN ('MANAGER', 'PARENT')    THEN 'PARENT'
                          WHEN u.role IN ('DEPENDENT', 'TEEN')    THEN 'TEEN'
                          WHEN u.role IN ('DOCTOR', 'THERAPIST')  THEN 'THERAPIST'
                          ELSE u.role
                      END,
    credits_balance = u.credits_balance,
    pin_code        = u.pin_code,
    account_type    = u.account_type,
    is_active       = u.is_active,
    last_login      = u.last_login,
    date_of_birth   = COALESCE(p.date_of_birth, u.dob),
    phone_number    = COALESCE(p.phone_number, u.phone_number)
FROM users u
WHERE u.user_id = p.user_id;

ALTER TABLE profiles ALTER COLUMN role SET NOT NULL;
ALTER TABLE profiles ADD CONSTRAINT uq_profiles_email UNIQUE (email);
ALTER TABLE profiles ADD CONSTRAINT profiles_role_check CHECK (role IN (
    'PARENT', 'TEEN', 'THERAPIST', 'ADMIN'
));
ALTER TABLE profiles ADD CONSTRAINT profiles_account_type_check CHECK (
    account_type IS NULL OR account_type IN ('PARENT', 'CHILD')
);

-- 4. Repoint refresh_tokens at profiles.
ALTER TABLE refresh_tokens ADD COLUMN profile_id UUID;

UPDATE refresh_tokens rt
SET profile_id = p.profile_id
FROM profiles p
WHERE p.user_id = rt.user_id;

-- Orphaned tokens (user deleted out-of-band) cannot survive the merge; drop them.
DELETE FROM refresh_tokens WHERE profile_id IS NULL;

ALTER TABLE refresh_tokens ALTER COLUMN profile_id SET NOT NULL;
ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (profile_id) ON DELETE CASCADE;
CREATE INDEX idx_refresh_tokens_profile_id ON refresh_tokens (profile_id);

DROP INDEX IF EXISTS idx_refresh_tokens_user_id;
ALTER TABLE refresh_tokens DROP COLUMN user_id;

-- 5. Drop the users table and the last user_id column.
ALTER TABLE profiles DROP CONSTRAINT fk_profiles_user;
ALTER TABLE profiles DROP COLUMN user_id;
DROP TABLE users;
