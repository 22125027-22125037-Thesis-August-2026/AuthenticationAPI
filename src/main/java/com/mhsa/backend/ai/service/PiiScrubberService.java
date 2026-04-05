package com.mhsa.backend.ai.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class PiiScrubberService {

    private static final String USER_PHONE_PLACEHOLDER = "[USER_PHONE]";
    private static final String USER_EMAIL_PLACEHOLDER = "[USER_EMAIL]";
    private static final String USER_NAME_PLACEHOLDER = "[USER_NAME]";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)\\b[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}\\b"
    );

    // Supports common Vietnamese mobile/landline formats with or without +84 and separators.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+84|84|0)(?:[235789])(?:[\\s.-]?\\d){8}(?!\\d)"
    );

    // Detect name when user introduces themselves: "Tôi tên là Nguyễn Văn A", "Mình là Tran Thi B"...
    private static final Pattern INTRODUCED_NAME_PATTERN = Pattern.compile(
            "(?iu)\\b(?:tôi tên là|toi ten la|mình tên là|minh ten la|em tên là|em ten la|"
            + "tên tôi là|ten toi la|tôi là|toi la|mình là|minh la|em là|em la|anh là|anh la|"
            + "chị là|chi la)\\s+([\\p{L}][\\p{L}\\p{M}']*(?:\\s+[\\p{L}][\\p{L}\\p{M}']*){1,3})"
    );

    // Detect standalone common Vietnamese full names (2-4 words) starting with known family names.
    private static final Pattern VIETNAMESE_FULL_NAME_PATTERN = Pattern.compile(
            "(?iu)\\b(?:nguyễn|nguyen|trần|tran|lê|le|phạm|pham|hoàng|hoang|huỳnh|huynh|"
            + "phan|vũ|vu|võ|vo|đặng|dang|bùi|bui|đỗ|do|hồ|ho|ngô|ngo|dương|duong|lý|ly)"
            + "\\s+[\\p{L}][\\p{L}\\p{M}']*(?:\\s+[\\p{L}][\\p{L}\\p{M}']*){1,2}\\b"
    );

    public String scrub(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return rawText;
        }

        String sanitized = rawText;
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll(USER_EMAIL_PLACEHOLDER);
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll(USER_PHONE_PLACEHOLDER);

        // Preserve introduction phrase while masking only the captured name segment.
        sanitized = INTRODUCED_NAME_PATTERN.matcher(sanitized)
                .replaceAll(matchResult -> {
                    String fullMatch = matchResult.group(0);
                    String nameGroup = matchResult.group(1);
                    return fullMatch.replace(nameGroup, USER_NAME_PLACEHOLDER);
                });

        sanitized = VIETNAMESE_FULL_NAME_PATTERN.matcher(sanitized).replaceAll(USER_NAME_PLACEHOLDER);

        return sanitized;
    }
}
