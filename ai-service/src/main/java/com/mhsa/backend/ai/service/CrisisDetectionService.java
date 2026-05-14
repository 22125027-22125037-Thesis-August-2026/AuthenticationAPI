package com.mhsa.backend.ai.service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.mhsa.backend.ai.exception.CrisisDetectedException;

@Service
public class CrisisDetectionService {

    private static final List<String> CRISIS_KEYWORDS = List.of(
            "tự tử",
            "tu tu",
            "muốn chết",
            "muon chet",
            "kết thúc cuộc đời",
            "ket thuc cuoc doi",
            "làm hại bản thân",
            "lam hai ban than",
            "hại bản thân",
            "hai ban than",
            "không muốn sống",
            "khong muon song",
            "tự sát",
            "tu sat",
            "giết người",
            "giet nguoi",
            "đâm người",
            "dam nguoi",
            "thảm sát",
            "tham sat"
    );

    private static final List<Pattern> CRISIS_PATTERNS = CRISIS_KEYWORDS.stream()
            .map(keyword -> Pattern.compile("(^|\\b)" + Pattern.quote(keyword) + "(\\b|$)", Pattern.CASE_INSENSITIVE))
            .toList();

    public boolean isCrisisDetected(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalizedMessage = normalizeForSearch(message);
        return CRISIS_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(normalizedMessage).find());
    }

    public void assertNoCrisis(String message) {
        if (isCrisisDetected(message)) {
            throw new CrisisDetectedException("Crisis keyword detected in user message");
        }
    }

    private String normalizeForSearch(String input) {
        String lowerCase = input.toLowerCase();
        String normalized = Normalizer.normalize(lowerCase, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }
}
