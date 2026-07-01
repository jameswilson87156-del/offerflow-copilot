package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("interview_prep")
public class InterviewPrepEntity {

    @TableId
    private String id;
    private String jobId;
    private String mode;
    private String positioningNotice;
    private String focusAreasJson;
    private String questionsJson;
    private String starDraftJson;
    private String riskNotesJson;
    private String reviewTimelineJson;
    private String reviewStatus;
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

    public String getPositioningNotice() {
        return positioningNotice;
    }

    public void setPositioningNotice(String positioningNotice) {
        this.positioningNotice = positioningNotice;
    }

    public String getFocusAreasJson() {
        return focusAreasJson;
    }

    public void setFocusAreasJson(String focusAreasJson) {
        this.focusAreasJson = focusAreasJson;
    }

    public String getQuestionsJson() {
        return questionsJson;
    }

    public void setQuestionsJson(String questionsJson) {
        this.questionsJson = questionsJson;
    }

    public String getStarDraftJson() {
        return starDraftJson;
    }

    public void setStarDraftJson(String starDraftJson) {
        this.starDraftJson = starDraftJson;
    }

    public String getRiskNotesJson() {
        return riskNotesJson;
    }

    public void setRiskNotesJson(String riskNotesJson) {
        this.riskNotesJson = riskNotesJson;
    }

    public String getReviewTimelineJson() {
        return reviewTimelineJson;
    }

    public void setReviewTimelineJson(String reviewTimelineJson) {
        this.reviewTimelineJson = reviewTimelineJson;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
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
