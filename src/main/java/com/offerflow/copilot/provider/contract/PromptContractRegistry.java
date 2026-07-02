package com.offerflow.copilot.provider.contract;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PromptContractRegistry {

    private static final List<String> COMMON_FORBIDDEN_CLAIMS = List.of(
            "Offer 概率",
            "录取概率",
            "保证通过",
            "实时面试代答",
            "生产级稳定接入真实模型",
            "真实用户或真实客户背书");

    private final Map<ProviderTaskType, PromptContract> contracts;

    public PromptContractRegistry() {
        EnumMap<ProviderTaskType, PromptContract> next = new EnumMap<>(ProviderTaskType.class);
        register(next, ProviderTaskType.JD_ANALYSIS, "jd-analysis-prompt-v1", "jd-analysis-schema-v1",
                "JdAnalysisResponse", List.of("jobTitle", "jdText", "sourceNote"),
                "Extract requirements from sanitized JD text and keep all output review-gated.");
        register(next, ProviderTaskType.EVIDENCE_BINDING, "evidence-binding-prompt-v1", "evidence-binding-schema-v1",
                "EvidenceBindingResponse", List.of("jobRequirements", "resumeEvidenceRefs", "bindingPolicy"),
                "Bind requirements to verified resume evidence without inventing experience.");
        register(next, ProviderTaskType.MATCH_REPORT, "match-report-prompt-v1", "match-report-schema-v1",
                "MatchReportResponse", List.of("jobRequirements", "evidenceBindings", "reviewStatus"),
                "Create a draft match report with evidence coverage, not admission predictions.");
        register(next, ProviderTaskType.INTERVIEW_PREP, "interview-prep-prompt-v1", "interview-prep-schema-v1",
                "InterviewPrepResponse", List.of("roleFocus", "resumeEvidenceRefs", "riskBoundaries"),
                "Prepare offline interview practice notes; never provide real-time interview assistance.");
        register(next, ProviderTaskType.OPENING_MESSAGE, "opening-message-prompt-v1", "opening-message-schema-v1",
                "OpeningMessageResponse", List.of("roleContext", "candidateEvidence", "channelBoundary"),
                "Draft a review-gated opening message without scraping or platform automation.");
        register(next, ProviderTaskType.HUMAN_REVIEW_REWRITE, "human-review-rewrite-prompt-v1", "human-review-rewrite-schema-v1",
                "HumanReviewRewriteResponse", List.of("draftText", "reviewNotes", "riskFlags"),
                "Rewrite only for clarity after human review and preserve all safety boundaries.");
        register(next, ProviderTaskType.PROVIDER_SANDBOX, "provider-sandbox-prompt-v2", "provider-sandbox-v1",
                "ProviderSandboxResponse", List.of("inputText", "providerMode", "riskPolicy"),
                "Return JSON only with schemaVersion=provider-sandbox-v1, taskType=PROVIDER_SANDBOX, answer, summary, riskFlags, humanReviewRequired=true, copyAllowed=false, and boundaryNotice.");
        contracts = Map.copyOf(next);
    }

    public List<PromptContract> all() {
        return ProviderTaskType.stream()
                .map(contracts::get)
                .toList();
    }

    public List<PromptContractSummary> summaries(RiskPolicyRegistry riskPolicyRegistry) {
        return all().stream()
                .map(contract -> {
                    RiskPolicy policy = riskPolicyRegistry.get(contract.taskType());
                    return new PromptContractSummary(
                            contract.taskType(),
                            contract.taskType().displayName(),
                            contract.promptVersion(),
                            contract.schemaVersion(),
                            contract.riskPolicyVersion(),
                            policy.requireHumanReview(),
                            contract.outputSchemaName(),
                            contract.boundaryNotice());
                })
                .toList();
    }

    public PromptContract get(ProviderTaskType taskType) {
        PromptContract contract = contracts.get(taskType);
        if (contract == null) {
            throw new IllegalArgumentException("No PromptContract for " + taskType);
        }
        return contract;
    }

    public PromptContract get(String taskType) {
        return get(ProviderTaskType.from(taskType));
    }

    private void register(
            EnumMap<ProviderTaskType, PromptContract> target,
            ProviderTaskType taskType,
            String promptVersion,
            String schemaVersion,
            String outputSchemaName,
            List<String> requiredInputs,
            String taskInstruction) {
        String riskPolicyVersion = "provider-risk-policy-v1";
        target.put(taskType, new PromptContract(
                taskType,
                promptVersion,
                schemaVersion,
                riskPolicyVersion,
                "You are OfferFlow Copilot operating under provider contract validation. Return only review-gated structured output.",
                taskInstruction + " Required inputs: {{requiredInputs}}. Output schema: " + outputSchemaName + ".",
                requiredInputs,
                COMMON_FORBIDDEN_CLAIMS,
                outputSchemaName,
                "Provider contract sandbox only; no real external model calls and all output requires Human Review."));
    }
}
