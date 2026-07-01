package com.offerflow.copilot.copy;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class CopyPermissionPolicy {

    public static final String BOUNDARY_NOTICE =
            "Schema Validate 与 Risk Guard 通过后仍需 Human Review Confirmed；Draft/In Review/Returned/Risk Flagged/Archived 不可复制。当前为 demo/local-rule/no-op，不承诺 Offer 结果，也不是实时面试辅助工具。";

    public CopyPermissionResult evaluate(CopyPermissionRequest request, CopyTargetSnapshot target) {
        String targetStatus = normalizeStatus(target.targetStatus());
        String humanReviewStatus = valueOr(target.humanReviewStatus(), displayStatus(targetStatus));
        String normalizedHumanReviewStatus = normalizeStatus(humanReviewStatus);
        boolean targetConfirmed = "CONFIRMED".equals(targetStatus);
        boolean humanConfirmed = "CONFIRMED".equals(normalizedHumanReviewStatus);
        boolean confirmed = targetConfirmed && humanConfirmed;

        Decision decision = decision(targetStatus, target.schemaValidated(), target.riskGuardPassed(), confirmed);
        String copyText = decision.allowed() ? valueOr(request.requestedText(), target.copyText()) : "";
        return new CopyPermissionResult(
                decision.allowed(),
                decision.reason(),
                target.targetType(),
                target.targetId(),
                targetStatus,
                humanReviewStatus,
                target.schemaValidated(),
                target.riskGuardPassed(),
                confirmed,
                BOUNDARY_NOTICE,
                "",
                copyText);
    }

    private Decision decision(String targetStatus, boolean schemaValidated, boolean riskGuardPassed, boolean confirmed) {
        if (!schemaValidated) {
            return new Decision(false, "Schema Validate 未通过");
        }
        if (!riskGuardPassed) {
            return new Decision(false, "Risk Guard 未通过");
        }
        if ("ARCHIVED".equals(targetStatus)) {
            return new Decision(false, "已归档");
        }
        if ("RISK_FLAGGED".equals(targetStatus)) {
            return new Decision(false, "存在风险");
        }
        if (!confirmed) {
            return new Decision(false, statusReason(targetStatus));
        }
        return new Decision(true, "已通过人工复核，可复制使用。");
    }

    private String statusReason(String status) {
        return switch (status) {
            case "DRAFT" -> "需要人工复核";
            case "IN_REVIEW" -> "正在复核";
            case "RETURNED" -> "已退回";
            case "RISK_FLAGGED" -> "存在风险";
            case "ARCHIVED" -> "已归档";
            case "CONFIRMED" -> "Human Review 尚未 Confirmed";
            default -> "未知状态，需人工复核";
        };
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "DRAFT";
        }
        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        if (normalized.contains("RISK")) {
            return "RISK_FLAGGED";
        }
        if (normalized.contains("IN_REVIEW")) {
            return "IN_REVIEW";
        }
        if (normalized.contains("CONFIRMED")) {
            return "CONFIRMED";
        }
        if (normalized.contains("RETURNED") || normalized.contains("RETURN")) {
            return "RETURNED";
        }
        if (normalized.contains("ARCHIVED") || normalized.contains("ARCHIVE")) {
            return "ARCHIVED";
        }
        if (normalized.contains("DRAFT") || normalized.contains("NEEDS_REVIEW")) {
            return "DRAFT";
        }
        return normalized;
    }

    private String displayStatus(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Confirmed";
            case "IN_REVIEW" -> "In Review";
            case "RETURNED" -> "Returned";
            case "RISK_FLAGGED" -> "Risk Flagged";
            case "ARCHIVED" -> "Archived";
            default -> "Draft";
        };
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private record Decision(boolean allowed, String reason) {
    }
}
