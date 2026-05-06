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
