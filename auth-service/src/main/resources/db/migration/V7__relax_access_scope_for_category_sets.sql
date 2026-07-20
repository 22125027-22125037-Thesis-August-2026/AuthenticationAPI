-- A data-access grant's access_scope is now a SET of tracking-category tokens, stored as a
-- comma-separated list in the single column (e.g. 'READ_SLEEP,READ_FOOD' or the 'READ_ALL'
-- shorthand). This lets one grant share any subset of a user's tracking data — e.g. sleep and
-- food, but not the journal — while keeping one grant row per (granter, grantee) pair.
--
-- 1. Drop the old two-value CHECK ('READ_JOURNAL','READ_ALL'); a CSV set can't be a simple IN list,
--    and token validity is enforced in the application (see contract.AccessScopes).
-- 2. Widen the column so a full category set fits.
--
-- Existing rows are all 'READ_ALL' (V2 seed) and remain valid verbatim.

ALTER TABLE data_access_grants DROP CONSTRAINT IF EXISTS chk_dag_scope;

ALTER TABLE data_access_grants ALTER COLUMN access_scope TYPE VARCHAR(100);
