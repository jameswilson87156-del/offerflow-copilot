package com.offerflow.copilot.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import org.springframework.stereotype.Service;

@Service
public class EvidenceLibraryService {

    private static final String MODE = "mock/local-rule";

    private final ResumeEvidenceRepository resumeEvidenceRepository;
    private final JsonCodec jsonCodec;

    public EvidenceLibraryService(ResumeEvidenceRepository resumeEvidenceRepository, JsonCodec jsonCodec) {
        this.resumeEvidenceRepository = resumeEvidenceRepository;
        this.jsonCodec = jsonCodec;
    }

    public EvidenceLibrary getLibrary() {
        List<ResumeEvidenceEntity> rows = resumeEvidenceRepository.findAll();
        return new EvidenceLibrary(
                MODE,
                rows.size(),
                categories(rows),
                rows.stream().map(this::item).toList(),
                "证据库从 H2 seeded demo data 读取，仅使用匿名化演示项目数据，不代表真实客户、用户、线上流量或生产级效果。");
    }

    public EvidenceCoverage getCoverage() {
        List<ResumeEvidenceEntity> rows = resumeEvidenceRepository.findAll();
        return new EvidenceCoverage(
                MODE,
                List.of(
                        coverage(rows, "Java", "强支撑", 92, "补充复杂并发案例"),
                        coverage(rows, "Spring Boot", "强支撑", 94, "补充事务压测记录"),
                        coverage(rows, "Vue 3", "中支撑", 68, "缺少组件测试证据"),
                        coverage(rows, "AI Workflow", "强支撑", 88, "需人工核验编排边界"),
                        coverage(rows, "RAG", "中支撑", 72, "缺少大规模离线评测"),
                        coverage(rows, "Trace", "强支撑", 86, "补充失败链路样例"),
                        coverage(rows, "CI", "中支撑", 74, "缺少发布审批记录"),
                        coverage(rows, "Deployment", "弱支撑", 46, "仅有演示部署，不代表生产运维")),
                "覆盖度由确定性 local-rule 根据数据库内的脱敏证据类型与人工确认状态计算，不是岗位录取概率。保持空缺比虚构证据更重要。");
    }

    private List<EvidenceLibrary.EvidenceCategory> categories(List<ResumeEvidenceEntity> rows) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("all", "全部证据");
        labels.put("java", "Java 后端");
        labels.put("ai-app", "AI 应用开发");
        labels.put("agent", "AI Agent 工具");
        labels.put("rag", "RAG / Knowledge");
        labels.put("ai-coding", "AI Coding");
        labels.put("frontend", "前端工程");
        labels.put("ci", "CI / 部署");
        labels.put("trace", "Trace / Human Review");
        return labels.entrySet().stream()
                .map((entry) -> new EvidenceLibrary.EvidenceCategory(
                        entry.getKey(),
                        entry.getValue(),
                        "all".equals(entry.getKey()) ? rows.size() : countTag(rows, entry.getValue())))
                .toList();
    }

    private int countTag(List<ResumeEvidenceEntity> rows, String tag) {
        return (int) rows.stream()
                .filter((row) -> readList(row.getAbilityTagsJson()).contains(tag))
                .count();
    }

    private EvidenceLibrary.EvidenceItem item(ResumeEvidenceEntity row) {
        return new EvidenceLibrary.EvidenceItem(
                row.getId(),
                row.getProjectName(),
                row.getProjectSlug(),
                row.getSummary(),
                readList(row.getAbilityTagsJson()),
                readList(row.getEvidenceSourcesJson()),
                row.getStrength(),
                readList(row.getMatchableRequirementsJson()),
                row.getReviewStatus(),
                row.getUpdatedAt().toLocalDate().toString(),
                jsonCodec.read(row.getDetailJson(), EvidenceLibrary.EvidenceDetail.class));
    }

    private EvidenceCoverage.CoverageItem coverage(
            List<ResumeEvidenceEntity> rows,
            String skill,
            String level,
            int score,
            String gap) {
        List<String> projects = rows.stream()
                .filter((row) -> supports(row, skill))
                .map(ResumeEvidenceEntity::getProjectName)
                .toList();
        return new EvidenceCoverage.CoverageItem(skill, level, score, projects, gap);
    }

    private boolean supports(ResumeEvidenceEntity row, String skill) {
        String needle = skill.toLowerCase();
        return readList(row.getSkillsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle))
                || readList(row.getAbilityTagsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle))
                || readList(row.getMatchableRequirementsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle));
    }

    private List<String> readList(String json) {
        return jsonCodec.readList(json, String.class);
    }
}
