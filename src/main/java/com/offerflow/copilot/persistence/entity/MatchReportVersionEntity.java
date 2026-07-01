package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("match_report_version")
public class MatchReportVersionEntity {

    @TableId
    private String id;
    private String reportId;
    private String jobId;
    private String parseVersionId;
    private Integer versionNo;
    private Integer score;
    private Integer skillScore;
    private Integer evidenceScore;
    private Integer riskScore;
    private Integer interviewScore;
    private String recommendedResume;
    private String status;
    private String summaryJson;
    private String scoreBreakdownJson;
    private String evidenceRefsJson;
    private String skillGapsJson;
    private String recommendedActionsJson;
    private String riskNotesJson;
    private String generatedBy;
    private String providerMode;
    private String promptVersion;
    private String schemaVersion;
    private String traceId;
    private String humanReviewId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
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

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(Integer skillScore) {
        this.skillScore = skillScore;
    }

    public Integer getEvidenceScore() {
        return evidenceScore;
    }

    public void setEvidenceScore(Integer evidenceScore) {
        this.evidenceScore = evidenceScore;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Integer getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(Integer interviewScore) {
        this.interviewScore = interviewScore;
    }

    public String getRecommendedResume() {
        return recommendedResume;
    }

    public void setRecommendedResume(String recommendedResume) {
        this.recommendedResume = recommendedResume;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummaryJson() {
        return summaryJson;
    }

    public void setSummaryJson(String summaryJson) {
        this.summaryJson = summaryJson;
    }

    public String getScoreBreakdownJson() {
        return scoreBreakdownJson;
    }

    public void setScoreBreakdownJson(String scoreBreakdownJson) {
        this.scoreBreakdownJson = scoreBreakdownJson;
    }

    public String getEvidenceRefsJson() {
        return evidenceRefsJson;
    }

    public void setEvidenceRefsJson(String evidenceRefsJson) {
        this.evidenceRefsJson = evidenceRefsJson;
    }

    public String getSkillGapsJson() {
        return skillGapsJson;
    }

    public void setSkillGapsJson(String skillGapsJson) {
        this.skillGapsJson = skillGapsJson;
    }

    public String getRecommendedActionsJson() {
        return recommendedActionsJson;
    }

    public void setRecommendedActionsJson(String recommendedActionsJson) {
        this.recommendedActionsJson = recommendedActionsJson;
    }

    public String getRiskNotesJson() {
        return riskNotesJson;
    }

    public void setRiskNotesJson(String riskNotesJson) {
        this.riskNotesJson = riskNotesJson;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getProviderMode() {
        return providerMode;
    }

    public void setProviderMode(String providerMode) {
        this.providerMode = providerMode;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getHumanReviewId() {
        return humanReviewId;
    }

    public void setHumanReviewId(String humanReviewId) {
        this.humanReviewId = humanReviewId;
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
