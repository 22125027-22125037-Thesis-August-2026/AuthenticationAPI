package com.mhsa.backend.contract;

import java.util.UUID;

/**
 * Reserved, non-human profile identities that participate in the platform's data model without ever
 * logging in. They exist so system actors can be represented as ordinary rows (e.g. a grantee in
 * {@code data_access_grants}) rather than requiring special-case columns or code paths.
 */
public final class SystemProfiles {

    /**
     * The uMatter AI companion, modelled as a grantee in the data-access-grant table. A user's
     * tracking data is fed to the AI for grounding <em>only</em> while an active grant from that user
     * to this principal exists — exactly like a therapist, parent, or friend. A reserved profile row
     * with this id is seeded in auth-service so the grantee foreign key is satisfied.
     */
    public static final UUID AI_COMPANION_ID = UUID.fromString("a1000000-a1a1-4a1a-8a1a-a1a1a1a1a1a1");

    private SystemProfiles() {
    }
}
