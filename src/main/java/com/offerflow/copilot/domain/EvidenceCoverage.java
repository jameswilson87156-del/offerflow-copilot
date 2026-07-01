package com.offerflow.copilot.domain;

import java.util.List;

public record EvidenceCoverage(
        String mode,
        List<CoverageItem> items,
        String note) {

    public record CoverageItem(
            String skill,
            String level,
            int score,
            List<String> supportingProjects,
            String gap) {
    }
}
