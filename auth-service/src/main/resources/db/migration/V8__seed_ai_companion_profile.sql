-- Reserved system profile for the AI companion (see contract.SystemProfiles.AI_COMPANION_ID).
--
-- The AI companion is modelled as an ordinary grantee in data_access_grants: a user's tracking data
-- is fed to the AI for grounding ONLY while an active grant from that user to this principal exists,
-- exactly like a therapist, parent, or friend. Because grantee_profile_id has a foreign key to
-- profiles, that row must exist for such a grant to be insertable.
--
-- This account can never log in: password is NULL and is_active is FALSE. profile_type 'Profile'
-- (the base discriminator) keeps it out of the teen and therapist directories, and it is never
-- published to other services.

INSERT INTO profiles (
    profile_id, full_name, profile_type, role, email, password, is_active, created_at, updated_at
) VALUES (
    'a1000000-a1a1-4a1a-8a1a-a1a1a1a1a1a1',
    'uMatter AI Companion',
    'Profile',
    'TEEN',
    'ai-companion@system.umatter.local',
    NULL,
    FALSE,
    now(),
    now()
)
ON CONFLICT (profile_id) DO NOTHING;
