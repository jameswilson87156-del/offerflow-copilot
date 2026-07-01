package com.offerflow.copilot.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.offerflow.copilot.domain.ApplicationTracker;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ApplicationRecordEntity;
import com.offerflow.copilot.persistence.repository.ApplicationRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTrackerService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ApplicationRecordRepository applicationRecordRepository;
    private final JsonCodec jsonCodec;

    public ApplicationTrackerService(ApplicationRecordRepository applicationRecordRepository, JsonCodec jsonCodec) {
        this.applicationRecordRepository = applicationRecordRepository;
        this.jsonCodec = jsonCodec;
    }

    public ApplicationTracker getTracker() {
        List<ApplicationRecordEntity> rows = applicationRecordRepository.findAll();
        return new ApplicationTracker(
                "mock/local-rule",
                boardColumns(rows),
                rows.stream().map(this::record).toList(),
                rows.stream()
                        .flatMap((row) -> jsonCodec.readList(row.getTimelineJson(), ApplicationTracker.CommunicationLog.class).stream())
                        .toList(),
                List.of(
                        "不自动投递",
                        "不抓取平台聊天",
                        "不保存真实 HR 隐私",
                        "不承诺回复率或 Offer 结果"),
                "投递跟踪从 H2 seeded demo data 读取，仅使用手动录入的匿名化演示记录，不接招聘平台 API，不做爬虫。");
    }

    private List<ApplicationTracker.BoardColumn> boardColumns(List<ApplicationRecordEntity> rows) {
        return List.of(
                column(rows, "not-applied", "未投递", "未投递", "neutral"),
                column(rows, "contacted", "已沟通", "已沟通", "info"),
                column(rows, "resume-sent", "已发送简历", "已发送简历", "primary"),
                column(rows, "interview", "已约面试", "已约面试", "positive"),
                column(rows, "following-up", "跟进中", "跟进中", "warning"),
                column(rows, "archived", "已拒绝 / 归档", "已拒绝", "muted"));
    }

    private ApplicationTracker.BoardColumn column(
            List<ApplicationRecordEntity> rows,
            String key,
            String label,
            String status,
            String tone) {
        int count = (int) rows.stream().filter((row) -> row.getStatus().contains(status)).count();
        return new ApplicationTracker.BoardColumn(key, label, count, tone);
    }

    private ApplicationTracker.ApplicationRecord record(ApplicationRecordEntity entity) {
        return new ApplicationTracker.ApplicationRecord(
                entity.getId(),
                entity.getCompany(),
                entity.getRoleTitle(),
                entity.getCity(),
                entity.getResumeVersion(),
                entity.getSourceNote(),
                entity.getStatus(),
                entity.getUpdatedAt().format(FORMATTER),
                entity.getNextAction());
    }
}
