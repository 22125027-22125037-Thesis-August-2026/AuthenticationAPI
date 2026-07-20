-- Mirror of auth-service's grant-scope change: access_scope is now a comma-separated SET of
-- tracking-category tokens (e.g. 'READ_SLEEP,READ_FOOD' or 'READ_ALL'), replicated verbatim from
-- auth. Widen the replica column so a full category set fits. This table has no CHECK constraint on
-- access_scope, and existing rows ('READ_ALL') remain valid.

ALTER TABLE data_access_grants ALTER COLUMN access_scope TYPE VARCHAR(100);
