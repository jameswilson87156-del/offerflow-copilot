package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.MatchReportAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.MatchReportAuditEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MatchReportAuditEventRepository {

    private final MatchReportAuditEventMapper mapper;

    public MatchReportAuditEventRepository(MatchReportAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(MatchReportAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<MatchReportAuditEventEntity> findByReportVersionId(String reportVersionId) {
        return mapper.selectList(Wrappers.<MatchReportAuditEventEntity>lambdaQuery()
                .eq(MatchReportAuditEventEntity::getReportVersionId, reportVersionId)
                .orderByAsc(MatchReportAuditEventEntity::getCreatedAt));
    }
}
