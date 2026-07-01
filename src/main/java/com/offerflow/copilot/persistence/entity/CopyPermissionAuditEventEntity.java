package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("copy_permission_audit_event")
public class CopyPermissionAuditEventEntity {

    @TableId
    private String id;
    private String targetType;
    private String targetId;
    private String action;
    private Boolean allowed;
    private String reason;
    private String targetStatus;
    private String humanReviewStatus;
    private Boolean schemaValidated;
    private Boolean riskGuardPassed;
    private String actor;
    private String actorRole;
    private String traceId;
    private String providerRunId;
    private String boundaryNotice;
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getHumanReviewStatus() {
        return humanReviewStatus;
    }

    public void setHumanReviewStatus(String humanReviewStatus) {
        this.humanReviewStatus = humanReviewStatus;
    }

    public Boolean getSchemaValidated() {
        return schemaValidated;
    }

    public void setSchemaValidated(Boolean schemaValidated) {
        this.schemaValidated = schemaValidated;
    }

    public Boolean getRiskGuardPassed() {
        return riskGuardPassed;
    }

    public void setRiskGuardPassed(Boolean riskGuardPassed) {
        this.riskGuardPassed = riskGuardPassed;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getProviderRunId() {
        return providerRunId;
    }

    public void setProviderRunId(String providerRunId) {
        this.providerRunId = providerRunId;
    }

    public String getBoundaryNotice() {
        return boundaryNotice;
    }

    public void setBoundaryNotice(String boundaryNotice) {
        this.boundaryNotice = boundaryNotice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
