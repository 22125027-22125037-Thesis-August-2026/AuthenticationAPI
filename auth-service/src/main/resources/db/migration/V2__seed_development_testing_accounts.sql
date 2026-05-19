-- Seed deterministic development/testing accounts for auth and profile tables.
-- Plaintext password: password
-- BCrypt hash: $2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS

-- 30 TEEN users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'teen' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'TEEN',
    (
        (ARRAY['Anh','Binh','Chi','Dung','Giang','Hoa','Khanh','Linh','Minh','Nam','Oanh','Phuc','Quang','Trang','Vy'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 3) % 10) + 1]
    ),
    now() - ((90 - n) || ' days')::interval,
    now() - ((90 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- 30 PARENT users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'parent' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'PARENT',
    (
        (ARRAY['An','Bao','Cuong','Diep','Hanh','Hung','Kiet','Lan','My','Nghia','Phuong','Quyen','Son','Thao','Yen'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 5) % 10) + 1]
    ),
    now() - ((120 - n) || ' days')::interval,
    now() - ((120 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- 30 THERAPIST users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'therapist' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'THERAPIST',
    (
        (ARRAY['Dr. Bao','Dr. Chau','Dr. Duy','Dr. Ha','Dr. Khoa','Dr. Lam','Dr. Mai','Dr. Nhi','Dr. Phat','Dr. Thu'])[(n % 10) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 7) % 10) + 1]
    ),
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- Base profiles for 30 TEEN accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Anh','Binh','Chi','Dung','Giang','Hoa','Khanh','Linh','Minh','Nam','Oanh','Phuc','Quang','Trang','Vy'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 3) % 10) + 1]
    ),
    'TEEN',
    now() - ((90 - n) || ' days')::interval,
    now() - ((90 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 PARENT accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['An','Bao','Cuong','Diep','Hanh','Hung','Kiet','Lan','My','Nghia','Phuong','Quyen','Son','Thao','Yen'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 5) % 10) + 1]
    ),
    'PARENT',
    now() - ((120 - n) || ' days')::interval,
    now() - ((120 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 THERAPIST accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Dr. Bao','Dr. Chau','Dr. Duy','Dr. Ha','Dr. Khoa','Dr. Lam','Dr. Mai','Dr. Nhi','Dr. Phat','Dr. Thu'])[(n % 10) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 7) % 10) + 1]
    ),
    'THERAPIST',
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- TEEN profile details (school, emergency contact)
INSERT INTO teen_profile (profile_id, school, emergency_contact)
SELECT
    (regexp_replace(md5('dev-profile-teen-' || (n)::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'High School ' || (n)::text,
    'Parent Contact ' || (n)::text
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Enhanced profile details for teen001 (Anh Nguyen)
UPDATE teen_profile
SET school = 'Trường THPT Nguyễn Tất Thành, Quận 1, TP.HCM',
    emergency_contact = 'Mẹ: Nguyễn Thị An | Số điện thoại: 0912.345.678'
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid;

-- THERAPIST profile details (specialization, bio, experience, fee)
INSERT INTO therapist_profile (profile_id, specialization, bio, years_of_experience, consultation_fee, is_verified)
SELECT
    (regexp_replace(md5('dev-profile-therapist-' || (n)::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'Child Psychology',
    'Experienced therapist',
    ((n % 20) + 5)::integer,
    (50.0)::numeric(12, 2),
    TRUE
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- DATA ACCESS GRANTS: Parents can view teen diaries/data
INSERT INTO data_access_grants (grant_id, granter_profile_id, grantee_profile_id, status, access_scope, granted_at, expires_at)
SELECT
    (regexp_replace(md5('dev-grant-teen-' || t.n::text || '-parent-' || p.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-profile-teen-' || t.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-profile-parent-' || p.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'ACTIVE',
    'READ_ALL',
    now() - ((60 - (t.n + p.n)) || ' days')::interval,
    null
FROM generate_series(1, 30) AS t(n)
CROSS JOIN generate_series(1, 2) AS p(n)
WHERE (t.n + p.n) % 3 = 0
ON CONFLICT DO NOTHING;

-- DATA ACCESS GRANTS: Therapists can view assigned teen data
INSERT INTO data_access_grants (grant_id, granter_profile_id, grantee_profile_id, status, access_scope, granted_at, expires_at)
SELECT
    (regexp_replace(md5('dev-grant-teen-' || t.n::text || '-therapist-' || th.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-profile-teen-' || t.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-profile-therapist-' || th.n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'ACTIVE',
    'READ_ALL',
    now() - ((45 - (t.n + th.n)) || ' days')::interval,
    null
FROM generate_series(1, 30) AS t(n)
CROSS JOIN generate_series(1, 3) AS th(n)
WHERE (t.n + th.n) % 4 = 0
ON CONFLICT DO NOTHING;
