package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.ApplicationTracker;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTrackerService {

    public ApplicationTracker getTracker() {
        return new ApplicationTracker(
                "mock/local-rule",
                List.of(
                        column("not-applied", "未投递", 1, "neutral"),
                        column("contacted", "已沟通", 1, "info"),
                        column("resume-sent", "已发送简历", 1, "primary"),
                        column("interview", "已约面试", 0, "positive"),
                        column("following-up", "跟进中", 1, "warning"),
                        column("archived", "已拒绝 / 归档", 0, "muted")),
                applications(),
                communicationLogs(),
                List.of(
                        "不自动投递",
                        "不抓取平台聊天",
                        "不保存真实 HR 隐私",
                        "不承诺回复率或 Offer 结果"),
                "投递跟踪仅使用手动录入的匿名化演示记录，不接招聘平台 API，不做爬虫。");
    }

    private List<ApplicationTracker.ApplicationRecord> applications() {
        return List.of(
                record(
                        "app-java-ai",
                        "科技创新公司",
                        "Java AI 应用开发实习生",
                        "上海",
                        "Java 后端版",
                        "手动记录：来自公开岗位描述摘要",
                        "已发送简历",
                        "2026-07-01 14:30",
                        "等待反馈，准备 Spring Boot 与 Trace Evidence 追问"),
                record(
                        "app-ai-coding",
                        "AI 工具公司",
                        "AI Coding 工具开发实习",
                        "杭州",
                        "AI Coding 版",
                        "手动记录：作品集匹配度较高",
                        "跟进中",
                        "2026-07-01 13:20",
                        "补充 Provider fallback 与 Human Review 说明"),
                record(
                        "app-java-backend",
                        "企业软件公司",
                        "Java 后端实习",
                        "北京",
                        "Java 后端版",
                        "手动记录：偏后端基础与 Redis",
                        "已沟通",
                        "2026-06-30 18:10",
                        "补齐 Redis 与分布式事务准备材料"));
    }

    private List<ApplicationTracker.CommunicationLog> communicationLogs() {
        return List.of(
                log("app-java-ai", "初始沟通", "确认岗位关注 Java、Spring Boot 与 AI 应用项目表达。", "2026-07-01 10:12"),
                log("app-java-ai", "已发送简历", "使用 Java 后端版简历，附作品集链接说明需人工复核。", "2026-07-01 14:30"),
                log("app-ai-coding", "等待反馈", "手动记录跟进，不抓取平台聊天。", "2026-07-01 13:20"),
                log("app-java-backend", "面试安排", "待确认具体时间，准备 Redis 与事务追问。", "2026-06-30 18:10"),
                log("app-java-backend", "面试复盘", "尚未面试，复盘区保留为空白待人工填写。", "2026-06-30 18:15"));
    }

    private ApplicationTracker.BoardColumn column(String key, String label, int count, String tone) {
        return new ApplicationTracker.BoardColumn(key, label, count, tone);
    }

    private ApplicationTracker.ApplicationRecord record(
            String id,
            String company,
            String role,
            String city,
            String resumeVersion,
            String sourceNote,
            String status,
            String updatedAt,
            String nextAction) {
        return new ApplicationTracker.ApplicationRecord(id, company, role, city, resumeVersion, sourceNote, status, updatedAt, nextAction);
    }

    private ApplicationTracker.CommunicationLog log(String applicationId, String stage, String note, String timestamp) {
        return new ApplicationTracker.CommunicationLog(applicationId, stage, note, timestamp);
    }
}
