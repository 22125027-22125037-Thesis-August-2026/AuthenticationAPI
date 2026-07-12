package com.mhsa.backend.auth.service;

import java.util.List;
import java.util.Locale;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mhsa.backend.auth.client.TherapistApiClient;
import com.mhsa.backend.auth.messaging.AssignmentSnapshotItem;
import com.mhsa.backend.auth.model.AssignmentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Nightly reconciliation that heals event drift by converging the local read-model to
 * therapist-api's authoritative snapshot.
 *
 * <p><b>Fail-safe by construction:</b> the snapshot is fetched and validated in full <i>before</i>
 * any write. If the fetch errors, returns no rows, or contains a malformed row, the job aborts and
 * the replica is left untouched — a failed sync never wipes assignments. On a clean fetch the apply
 * step is idempotent, so a converged replica makes it a no-op.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentReconciliationService {

    private final TherapistApiClient therapistApiClient;
    private final AssignmentReplicaService replicaService;

    /** 23:00 Asia/Ho_Chi_Minh daily. */
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Ho_Chi_Minh")
    public void reconcile() {
        List<AssignmentSnapshotItem> snapshot;
        try {
            snapshot = therapistApiClient.fetchAllAssignments();
        } catch (Exception e) {
            log.error("Assignment reconciliation ABORTED: snapshot fetch failed; replica left untouched", e);
            return;
        }

        if (snapshot == null || snapshot.isEmpty()) {
            // Treat empty as suspicious (partial/failed fetch) rather than "deactivate everything".
            log.warn("Assignment reconciliation ABORTED: snapshot empty; replica left untouched");
            return;
        }

        // Validate up front — a malformed row means we can't trust snapshot completeness, so abort
        // before mutating anything.
        for (AssignmentSnapshotItem item : snapshot) {
            if (item == null || !item.isValid() || !isParseableStatus(item.status())) {
                log.error("Assignment reconciliation ABORTED: malformed snapshot row {}; replica left untouched", item);
                return;
            }
        }

        int[] counts = replicaService.reconcileSnapshot(snapshot);
        log.info("Assignment reconciliation complete: {} rows in snapshot, {} upserted, {} deactivated",
                snapshot.size(), counts[0], counts[1]);
    }

    private boolean isParseableStatus(String raw) {
        if (raw == null) {
            return false;
        }
        try {
            AssignmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
