package com.mhsa.backend.contract;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The vocabulary and parsing rules for a data-access grant's {@code accessScope}.
 *
 * <p>A grant's scope is a <strong>set</strong> of tracking categories, persisted in the single
 * {@code access_scope} column as a comma-separated list of tokens (e.g. {@code "READ_SLEEP,READ_FOOD"}).
 * The shorthand {@link #READ_ALL} means "every category, including any added later" and, when present,
 * subsumes the individual tokens. This keeps one grant row per (granter, grantee) pair — the whole
 * event/replica/reconcile/revoke pipeline is unchanged — while letting a user share any subset of
 * their tracking data (e.g. sleep and food, but not the journal).
 *
 * <p>Kept in shared-contracts so auth-service (which issues and validates grants) and tracking-service
 * (which enforces them) agree on the exact tokens byte-for-byte across the wire.
 */
public final class AccessScopes {

    /** Grants every category, present and future. Legacy grants and the AI companion use this. */
    public static final String READ_ALL = "READ_ALL";

    public static final String READ_JOURNAL = "READ_JOURNAL";
    public static final String READ_SLEEP = "READ_SLEEP";
    public static final String READ_FOOD = "READ_FOOD";
    public static final String READ_MOOD = "READ_MOOD";
    public static final String READ_STEPS = "READ_STEPS";
    public static final String READ_BREATHING = "READ_BREATHING";

    /**
     * The individual per-category tokens (excludes the {@link #READ_ALL} shorthand), in canonical
     * order — the order {@link #normalize} emits them.
     */
    public static final List<String> CATEGORY_TOKENS = List.of(
            READ_JOURNAL, READ_SLEEP, READ_FOOD, READ_MOOD, READ_STEPS, READ_BREATHING);

    private AccessScopes() {
    }

    /**
     * Splits a CSV scope string into its normalized (trimmed, upper-cased) tokens. Blank input yields
     * an empty set. Insertion order is preserved for a stable {@link #normalize} round-trip.
     */
    public static Set<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(token -> token.trim().toUpperCase(Locale.ROOT))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * A scope string is valid when it is non-empty and every token is a known category token or the
     * {@code READ_ALL} shorthand.
     */
    public static boolean isValid(String csv) {
        Set<String> tokens = parse(csv);
        if (tokens.isEmpty()) {
            return false;
        }
        return tokens.stream().allMatch(t -> t.equals(READ_ALL) || CATEGORY_TOKENS.contains(t));
    }

    /**
     * Whether a grant carrying {@code scopeCsv} permits reading {@code categoryToken}
     * (e.g. {@link #READ_SLEEP}). {@code READ_ALL} permits every category.
     */
    public static boolean allows(String scopeCsv, String categoryToken) {
        Set<String> tokens = parse(scopeCsv);
        return tokens.contains(READ_ALL) || tokens.contains(categoryToken.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Canonical form: {@code READ_ALL} alone if the shorthand is present, otherwise the known tokens
     * joined with commas in a stable order. Unknown tokens are dropped. Returns {@code null} when the
     * input has no valid tokens, so callers can reject it.
     */
    public static String normalize(String csv) {
        Set<String> tokens = parse(csv);
        if (tokens.contains(READ_ALL)) {
            return READ_ALL;
        }
        String joined = CATEGORY_TOKENS.stream()
                .filter(tokens::contains)
                .collect(Collectors.joining(","));
        return joined.isEmpty() ? null : joined;
    }
}
