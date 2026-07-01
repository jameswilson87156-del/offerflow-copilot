package com.offerflow.copilot.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("jd_parse_version")
public class JdParseVersionEntity {

    @TableId
    private String id;
    private String jobId;
    private Integer versionNo;
    private String parserMode;
    private String providerMode;
    private String promptVersion;
    private String schemaVersion;
    private String extractedRequirementsJson;
    private String keywordsJson;
    private String riskTermsJson;
    private String sanitizedText;
    private String parseStatus;
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

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getParserMode() {
        return parserMode;
    }

    public void setParserMode(String parserMode) {
        this.parserMode = parserMode;
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

    public String getExtractedRequirementsJson() {
        return extractedRequirementsJson;
    }

    public void setExtractedRequirementsJson(String extractedRequirementsJson) {
        this.extractedRequirementsJson = extractedRequirementsJson;
    }

    public String getKeywordsJson() {
        return keywordsJson;
    }

    public void setKeywordsJson(String keywordsJson) {
        this.keywordsJson = keywordsJson;
    }

    public String getRiskTermsJson() {
        return riskTermsJson;
    }

    public void setRiskTermsJson(String riskTermsJson) {
        this.riskTermsJson = riskTermsJson;
    }

    public String getSanitizedText() {
        return sanitizedText;
    }

    public void setSanitizedText(String sanitizedText) {
        this.sanitizedText = sanitizedText;
    }

    public String getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus) {
        this.parseStatus = parseStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
