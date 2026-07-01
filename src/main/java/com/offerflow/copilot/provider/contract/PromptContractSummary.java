package com.offerflow.copilot.provider.contract;

public record PromptContractSummary(
        ProviderTaskType taskType,
        String displayName,
        String promptVersion,
        String schemaVersion,
        String riskPolicyVersion,
        boolean requireHumanReview,
        String outputSchemaName,
        String boundaryNotice) {
}
