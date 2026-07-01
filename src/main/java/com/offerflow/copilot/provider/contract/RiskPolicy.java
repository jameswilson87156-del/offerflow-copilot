package com.offerflow.copilot.provider.contract;

import java.util.List;

public record RiskPolicy(
        String riskPolicyVersion,
        List<String> forbiddenTerms,
        List<String> forbiddenClaims,
        List<String> requiredBoundaryNotices,
        boolean requireHumanReview,
        boolean allowCopyOnlyAfterConfirmed) {

    public RiskPolicy {
        forbiddenTerms = forbiddenTerms == null ? List.of() : List.copyOf(forbiddenTerms);
        forbiddenClaims = forbiddenClaims == null ? List.of() : List.copyOf(forbiddenClaims);
        requiredBoundaryNotices = requiredBoundaryNotices == null ? List.of() : List.copyOf(requiredBoundaryNotices);
    }
}
