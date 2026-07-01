package com.offerflow.copilot.provider.contract;

import java.util.List;

public record ProviderResponseSchema(
        String schemaVersion,
        String schemaName,
        List<String> requiredFields,
        List<String> optionalFields,
        int maxTextLength,
        boolean allowRawText,
        boolean requireStructuredJson) {

    public ProviderResponseSchema {
        requiredFields = requiredFields == null ? List.of() : List.copyOf(requiredFields);
        optionalFields = optionalFields == null ? List.of() : List.copyOf(optionalFields);
    }
}
