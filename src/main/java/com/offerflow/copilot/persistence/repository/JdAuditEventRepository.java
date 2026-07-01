package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.JdAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.JdAuditEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdAuditEventRepository {

    private final JdAuditEventMapper mapper;

    public JdAuditEventRepository(JdAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(JdAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<JdAuditEventEntity> findByJobId(String jobId) {
        return mapper.selectList(Wrappers.<JdAuditEventEntity>lambdaQuery()
                .eq(JdAuditEventEntity::getJobId, jobId)
                .orderByAsc(JdAuditEventEntity::getCreatedAt));
    }
}
