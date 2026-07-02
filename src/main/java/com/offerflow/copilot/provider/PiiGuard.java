package com.offerflow.copilot.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PiiGuard {

    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern CN_MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern CN_ID_CARD = Pattern.compile("(?<!\\d)\\d{17}[0-9Xx](?!\\d)");
    private static final Pattern SECRET_KEY = Pattern.compile("(?i)\\b(?:sk-[A-Za-z0-9_-]{8,}|api[_ -]?key\\s*[:=]\\s*\\S+)");

    public PiiGuardResult inspect(String inputText) {
        String text = inputText == null ? "" : inputText;
        List<String> flags = new ArrayList<>();
        if (EMAIL.matcher(text).find()) {
            flags.add("pii-email");
        }
        if (CN_MOBILE.matcher(text).find()) {
            flags.add("pii-phone");
        }
        if (CN_ID_CARD.matcher(text).find()) {
            flags.add("pii-id-card");
        }
        if (SECRET_KEY.matcher(text).find()) {
            flags.add("pii-secret-key");
        }
        if (flags.isEmpty()) {
            return new PiiGuardResult(false, List.of("pii-guard-passed"), "PII Guard passed.");
        }
        return new PiiGuardResult(
                true,
                flags,
                "PII Guard blocked the dry-run input. Remove phone numbers, emails, ID cards, and API keys.");
    }
}
