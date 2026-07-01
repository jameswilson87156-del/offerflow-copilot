package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.ResumeEvidenceAuditEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ResumeEvidenceAuditEventRepository {

    private final ResumeEvidenceAuditEventMapper mapper;

    public ResumeEvidenceAuditEventRepository(ResumeEvidenceAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(ResumeEvidenceAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<ResumeEvidenceAuditEventEntity> findByEvidenceId(String evidenceId) {
        return mapper.selectList(Wrappers.<ResumeEvidenceAuditEventEntity>lambdaQuery()
                .eq(ResumeEvidenceAuditEventEntity::getEvidenceId, evidenceId)
                .orderByAsc(ResumeEvidenceAuditEventEntity::getCreatedAt));
    }
}
