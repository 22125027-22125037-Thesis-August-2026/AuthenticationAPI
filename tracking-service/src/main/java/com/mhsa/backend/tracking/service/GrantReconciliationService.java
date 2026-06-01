package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.Locale;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mhsa.backend.tracking.client.AuthGrantApiClient;
import com.mhsa.backend.tracking.entity.GrantStatus;
import com.mhsa.backend.tracking.messaging.GrantSnapshotItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Nightly reconciliation that heals event drift by converging the local grant replica to
 * auth-service's authoritative snapshot. Also backfills any grants that predate the event stream.
 *
 * <p><b>Fail-safe by construction:</b> the snapshot is fetched and validated in full <i>before</i>
 * any write. If the fetch errors, returns no rows, or contains a malformed row, the job aborts and
 * the replica is left untouched — a failed sync never wipes grants. On a clean fetch the apply step
 * is idempotent, so a converged replica makes it a no-op.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GrantReconciliationService {

    private final AuthGrantApiClient authGrantApiClient;
    private final GrantReplicaService replicaService;

    /** 23:30 Asia/Ho_Chi_Minh daily. */
    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Ho_Chi_Minh")
    public void reconcile() {
        List<GrantSnapshotItem> snapshot;
        try {
            snapshot = authGrantApiClient.fetchAllGrants();
        } catch (Exception e) {
            log.error("Grant reconciliation ABORTED: snapshot fetch failed; replica left untouched", e);
            return;
        }

        if (snapshot == null || snapshot.isEmpty()) {
            // Treat empty as suspicious (partial/failed fetch) rather than "revoke everything".
            log.warn("Grant reconciliation ABORTED: snapshot empty; replica left untouched");
            return;
        }

        // Validate up front — a malformed row means we can't trust snapshot completeness, so abort
        // before mutating anything.
        for (GrantSnapshotItem item : snapshot) {
            if (item == null || !item.isValid() || !isParseableStatus(item.status())) {
                log.error("Grant reconciliation ABORTED: malformed snapshot row {}; replica left untouched", item);
                return;
            }
        }

        int[] counts = replicaService.reconcileSnapshot(snapshot);
        log.info("Grant reconciliation complete: {} rows in snapshot, {} upserted, {} revoked",
                snapshot.size(), counts[0], counts[1]);
    }

    private boolean isParseableStatus(String raw) {
        if (raw == null) {
            return false;
        }
        try {
            GrantStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
