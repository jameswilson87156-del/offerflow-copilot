package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("match_report")
public class MatchReportEntity {

    @TableId
    private String id;
    private String jobId;
    private String mode;
    private Integer score;
    private Integer skillScore;
    private Integer evidenceScore;
    private Integer riskScore;
    private Integer interviewScore;
    private String recommendedResume;
    private String status;
    private String summaryJson;
    private String scoreJson;
    private String evidenceSourcesJson;
    private String skillGapsJson;
    private String recommendedActionsJson;
    private String traceEvidenceJson;
    private String disclaimer;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    public String getScoreJson() {
        return scoreJson;
    }

    public void setScoreJson(String scoreJson) {
        this.scoreJson = scoreJson;
    }

    public String getEvidenceSourcesJson() {
        return evidenceSourcesJson;
    }

    public void setEvidenceSourcesJson(String evidenceSourcesJson) {
        this.evidenceSourcesJson = evidenceSourcesJson;
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

    public String getTraceEvidenceJson() {
        return traceEvidenceJson;
    }

    public void setTraceEvidenceJson(String traceEvidenceJson) {
        this.traceEvidenceJson = traceEvidenceJson;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
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
