-- Seed deterministic development/testing accounts for auth and profile tables.
-- Scope: users, profiles, teen_profile, parent_profile, therapist_profile.
-- Notes:
-- 1) Inserts are deterministic and idempotent by primary key.
-- 2) Plaintext password for all generated accounts is: password
-- 3) Stored hash corresponds to BCrypt("password").

-- 30 TEEN users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    dob,
    phone_number,
    credits_balance,
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
    DATE '2008-01-01' + (n * 37),
    '+8491' || lpad((100000 + n)::text, 6, '0'),
    100 + (n * 10),
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
    dob,
    phone_number,
    credits_balance,
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
    DATE '1982-01-01' + (n * 120),
    '+8492' || lpad((200000 + n)::text, 6, '0'),
    1000 + (n * 50),
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
    dob,
    phone_number,
    credits_balance,
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
    DATE '1978-01-01' + (n * 180),
    '+8493' || lpad((300000 + n)::text, 6, '0'),
    0,
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- Base profiles for 30 TEEN accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    avatar_url,
    date_of_birth,
    phone_number,
    created_at,
    updated_at,
    profile_type
)
SELECT
    (regexp_replace(md5('dev-profile-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Anh','Binh','Chi','Dung','Giang','Hoa','Khanh','Linh','Minh','Nam','Oanh','Phuc','Quang','Trang','Vy'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 3) % 10) + 1]
    ),
    'https://images.mhsa.local/avatars/teen-' || lpad(n::text, 3, '0') || '.jpg',
    DATE '2008-01-01' + (n * 37),
    '+8491' || lpad((100000 + n)::text, 6, '0'),
    now() - ((90 - n) || ' days')::interval,
    now() - ((90 - n) || ' days')::interval,
    'TEEN'
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 PARENT accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    avatar_url,
    date_of_birth,
    phone_number,
    created_at,
    updated_at,
    profile_type
)
SELECT
    (regexp_replace(md5('dev-profile-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['An','Bao','Cuong','Diep','Hanh','Hung','Kiet','Lan','My','Nghia','Phuong','Quyen','Son','Thao','Yen'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 5) % 10) + 1]
    ),
    'https://images.mhsa.local/avatars/parent-' || lpad(n::text, 3, '0') || '.jpg',
    DATE '1982-01-01' + (n * 120),
    '+8492' || lpad((200000 + n)::text, 6, '0'),
    now() - ((120 - n) || ' days')::interval,
    now() - ((120 - n) || ' days')::interval,
    'PARENT'
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 THERAPIST accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    avatar_url,
    date_of_birth,
    phone_number,
    created_at,
    updated_at,
    profile_type
)
SELECT
    (regexp_replace(md5('dev-profile-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Dr. Bao','Dr. Chau','Dr. Duy','Dr. Ha','Dr. Khoa','Dr. Lam','Dr. Mai','Dr. Nhi','Dr. Phat','Dr. Thu'])[(n % 10) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 7) % 10) + 1]
    ),
    'https://images.mhsa.local/avatars/therapist-' || lpad(n::text, 3, '0') || '.jpg',
    DATE '1978-01-01' + (n * 180),
    '+8493' || lpad((300000 + n)::text, 6, '0'),
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval,
    'THERAPIST'
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Teen profile details
INSERT INTO teen_profile (
    profile_id,
    school,
    emergency_contact
)
SELECT
    (regexp_replace(md5('dev-profile-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (ARRAY[
        'Nguyen Du High School',
        'Le Quy Don High School',
        'Tran Phu High School',
        'Vo Thi Sau High School',
        'Gia Dinh High School',
        'Ly Thuong Kiet High School'
    ])[((n - 1) % 6) + 1],
    '+8490' || lpad((500000 + n)::text, 6, '0')
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Parent profile details (linked_teen_id points to the paired teen user)
INSERT INTO parent_profile (
    profile_id,
    linked_teen_id
)
SELECT
    (regexp_replace(md5('dev-profile-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Therapist profile details
INSERT INTO therapist_profile (
    profile_id,
    specialization,
    bio,
    years_of_experience,
    consultation_fee,
    is_verified
)
SELECT
    (regexp_replace(md5('dev-profile-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (ARRAY[
        'Cognitive Behavioral Therapy',
        'Adolescent Anxiety Support',
        'Family Counseling',
        'Trauma-Informed Care',
        'Mindfulness-Based Therapy',
        'School Stress Management'
    ])[((n - 1) % 6) + 1],
    'Experienced therapist focused on teen mental wellness, coping skills, and parent guidance. Case load slot #' || n::text,
    3 + (n % 18),
    (350000 + (n * 25000))::numeric(12, 2),
    (n % 3 <> 0)
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;
