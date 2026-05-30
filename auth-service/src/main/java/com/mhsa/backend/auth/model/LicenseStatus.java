package com.mhsa.backend.auth.model;

/**
 * License verification lifecycle for a therapist profile.
 *
 * <ul>
 *   <li>{@code PENDING_VERIFICATION} — a license/renewal document was submitted and is awaiting admin review.</li>
 *   <li>{@code VERIFIED} — an admin approved the license; the therapist may take bookings.</li>
 *   <li>{@code REJECTED} — an admin rejected the submitted document.</li>
 *   <li>{@code EXPIRED} — the license expiry date has passed.</li>
 * </ul>
 */
public enum LicenseStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    REJECTED,
    EXPIRED
}
