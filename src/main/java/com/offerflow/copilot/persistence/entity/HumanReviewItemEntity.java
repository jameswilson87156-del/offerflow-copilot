package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("human_review_item")
public class HumanReviewItemEntity {

    @TableId
    private String id;
    private String reviewType;
    private String title;
    private String riskLevel;
    private String sourcePage;
    private String providerMode;
    private String traceId;
    private String originalText;
    private String jdSnippet;
    private String riskTermsJson;
    private String evidenceRefsJson;
    private String status;
    private String reviewer;
    private String humanNote;
    private String evidenceNote;
    private String lastAction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(String sourcePage) {
        this.sourcePage = sourcePage;
    }

    public String getProviderMode() {
        return providerMode;
    }

    public void setProviderMode(String providerMode) {
        this.providerMode = providerMode;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getJdSnippet() {
        return jdSnippet;
    }

    public void setJdSnippet(String jdSnippet) {
        this.jdSnippet = jdSnippet;
    }

    public String getRiskTermsJson() {
        return riskTermsJson;
    }

    public void setRiskTermsJson(String riskTermsJson) {
        this.riskTermsJson = riskTermsJson;
    }

    public String getEvidenceRefsJson() {
        return evidenceRefsJson;
    }

    public void setEvidenceRefsJson(String evidenceRefsJson) {
        this.evidenceRefsJson = evidenceRefsJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getHumanNote() {
        return humanNote;
    }

    public void setHumanNote(String humanNote) {
        this.humanNote = humanNote;
    }

    public String getEvidenceNote() {
        return evidenceNote;
    }

    public void setEvidenceNote(String evidenceNote) {
        this.evidenceNote = evidenceNote;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
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
