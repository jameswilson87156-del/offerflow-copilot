package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("resume_evidence")
public class ResumeEvidenceEntity {

    @TableId
    private String id;
    private String projectName;
    private String projectSlug;
    private String category;
    private String summary;
    private String skillsJson;
    private String abilityTagsJson;
    private String evidenceSourcesJson;
    private String matchableRequirementsJson;
    private String detailJson;
    private String strength;
    private String reviewStatus;
    private String boundaryNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getAbilityTagsJson() {
        return abilityTagsJson;
    }

    public void setAbilityTagsJson(String abilityTagsJson) {
        this.abilityTagsJson = abilityTagsJson;
    }

    public String getEvidenceSourcesJson() {
        return evidenceSourcesJson;
    }

    public void setEvidenceSourcesJson(String evidenceSourcesJson) {
        this.evidenceSourcesJson = evidenceSourcesJson;
    }

    public String getMatchableRequirementsJson() {
        return matchableRequirementsJson;
    }

    public void setMatchableRequirementsJson(String matchableRequirementsJson) {
        this.matchableRequirementsJson = matchableRequirementsJson;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getBoundaryNote() {
        return boundaryNote;
    }

    public void setBoundaryNote(String boundaryNote) {
        this.boundaryNote = boundaryNote;
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
