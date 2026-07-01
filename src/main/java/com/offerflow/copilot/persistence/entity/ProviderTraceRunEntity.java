package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("provider_trace_run")
public class ProviderTraceRunEntity {

    @TableId
    private String id;
    private String runId;
    private String jobTitle;
    private String providerMode;
    private String finalProvider;
    private String model;
    private String fallbackReason;
    private String promptVersion;
    private String schemaVersion;
    private String riskFlagsJson;
    private Integer evidenceCount;
    private String humanReviewStatus;
    private Integer durationMs;
    private String traceHash;
    private String evidenceDetailJson;
    private String technicalTagsJson;
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getProviderMode() {
        return providerMode;
    }

    public void setProviderMode(String providerMode) {
        this.providerMode = providerMode;
    }

    public String getFinalProvider() {
        return finalProvider;
    }

    public void setFinalProvider(String finalProvider) {
        this.finalProvider = finalProvider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }

    public void setFallbackReason(String fallbackReason) {
        this.fallbackReason = fallbackReason;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getRiskFlagsJson() {
        return riskFlagsJson;
    }

    public void setRiskFlagsJson(String riskFlagsJson) {
        this.riskFlagsJson = riskFlagsJson;
    }

    public Integer getEvidenceCount() {
        return evidenceCount;
    }

    public void setEvidenceCount(Integer evidenceCount) {
        this.evidenceCount = evidenceCount;
    }

    public String getHumanReviewStatus() {
        return humanReviewStatus;
    }

    public void setHumanReviewStatus(String humanReviewStatus) {
        this.humanReviewStatus = humanReviewStatus;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getTraceHash() {
        return traceHash;
    }

    public void setTraceHash(String traceHash) {
        this.traceHash = traceHash;
    }

    public String getEvidenceDetailJson() {
        return evidenceDetailJson;
    }

    public void setEvidenceDetailJson(String evidenceDetailJson) {
        this.evidenceDetailJson = evidenceDetailJson;
    }

    public String getTechnicalTagsJson() {
        return technicalTagsJson;
    }

    public void setTechnicalTagsJson(String technicalTagsJson) {
        this.technicalTagsJson = technicalTagsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
