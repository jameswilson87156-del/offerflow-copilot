package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("jd_audit_event")
public class JdAuditEventEntity {

    @TableId
    private String id;
    private String jobId;
    private String action;
    private String previousStatus;
    private String nextStatus;
    private String actor;
    private String actorRole;
    private String changedFieldsJson;
    private String beforeSnapshotJson;
    private String afterSnapshotJson;
    private String humanNote;
    private LocalDateTime createdAt;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(String nextStatus) {
        this.nextStatus = nextStatus;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getChangedFieldsJson() {
        return changedFieldsJson;
    }

    public void setChangedFieldsJson(String changedFieldsJson) {
        this.changedFieldsJson = changedFieldsJson;
    }

    public String getBeforeSnapshotJson() {
        return beforeSnapshotJson;
    }

    public void setBeforeSnapshotJson(String beforeSnapshotJson) {
        this.beforeSnapshotJson = beforeSnapshotJson;
    }

    public String getAfterSnapshotJson() {
        return afterSnapshotJson;
    }

    public void setAfterSnapshotJson(String afterSnapshotJson) {
        this.afterSnapshotJson = afterSnapshotJson;
    }

    public String getHumanNote() {
        return humanNote;
    }

    public void setHumanNote(String humanNote) {
        this.humanNote = humanNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
