CREATE TABLE data_access_grants (
    grant_id UUID PRIMARY KEY,
    granter_profile_id UUID NOT NULL,
    grantee_profile_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    access_scope VARCHAR(20) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_data_access_grants_pair UNIQUE(granter_profile_id, grantee_profile_id)
);

CREATE INDEX idx_grants_granter ON data_access_grants(granter_profile_id);
CREATE INDEX idx_grants_grantee ON data_access_grants(grantee_profile_id);
CREATE INDEX idx_grants_status ON data_access_grants(status);
