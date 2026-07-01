package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("jd_evidence_binding")
public class JdEvidenceBindingEntity {

    @TableId
    private String id;
    private String jobId;
    private String parseVersionId;
    private String requirementKey;
    private String requirementLabel;
    private String evidenceId;
    private String evidenceStrength;
    private String bindingReason;
    private String evidenceSource;
    private String reviewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getParseVersionId() {
        return parseVersionId;
    }

    public void setParseVersionId(String parseVersionId) {
        this.parseVersionId = parseVersionId;
    }

    public String getRequirementKey() {
        return requirementKey;
    }

    public void setRequirementKey(String requirementKey) {
        this.requirementKey = requirementKey;
    }

    public String getRequirementLabel() {
        return requirementLabel;
    }

    public void setRequirementLabel(String requirementLabel) {
        this.requirementLabel = requirementLabel;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getEvidenceStrength() {
        return evidenceStrength;
    }

    public void setEvidenceStrength(String evidenceStrength) {
        this.evidenceStrength = evidenceStrength;
    }

    public String getBindingReason() {
        return bindingReason;
    }

    public void setBindingReason(String bindingReason) {
        this.bindingReason = bindingReason;
    }

    public String getEvidenceSource() {
        return evidenceSource;
    }

    public void setEvidenceSource(String evidenceSource) {
        this.evidenceSource = evidenceSource;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
