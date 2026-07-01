package com.offerflow.copilot.provider.contract;

import java.util.List;

public record PromptContract(
        ProviderTaskType taskType,
        String promptVersion,
        String schemaVersion,
        String riskPolicyVersion,
        String systemInstruction,
        String userInstructionTemplate,
        List<String> requiredInputs,
        List<String> forbiddenClaims,
        String outputSchemaName,
        String boundaryNotice) {

    public PromptContract {
        requiredInputs = requiredInputs == null ? List.of() : List.copyOf(requiredInputs);
        forbiddenClaims = forbiddenClaims == null ? List.of() : List.copyOf(forbiddenClaims);
    }
}
