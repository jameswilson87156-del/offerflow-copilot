package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.JdEvidenceBindingEntity;
import com.offerflow.copilot.persistence.mapper.JdEvidenceBindingMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdEvidenceBindingRepository {

    private final JdEvidenceBindingMapper mapper;

    public JdEvidenceBindingRepository(JdEvidenceBindingMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(JdEvidenceBindingEntity entity) {
        mapper.insert(entity);
    }

    public void deleteByJobIdAndParseVersionId(String jobId, String parseVersionId) {
        mapper.delete(Wrappers.<JdEvidenceBindingEntity>lambdaQuery()
                .eq(JdEvidenceBindingEntity::getJobId, jobId)
                .eq(JdEvidenceBindingEntity::getParseVersionId, parseVersionId));
    }

    public List<JdEvidenceBindingEntity> findByJobId(String jobId) {
        return mapper.selectList(Wrappers.<JdEvidenceBindingEntity>lambdaQuery()
                .eq(JdEvidenceBindingEntity::getJobId, jobId)
                .orderByAsc(JdEvidenceBindingEntity::getCreatedAt));
    }

    public List<JdEvidenceBindingEntity> findByParseVersionId(String parseVersionId) {
        return mapper.selectList(Wrappers.<JdEvidenceBindingEntity>lambdaQuery()
                .eq(JdEvidenceBindingEntity::getParseVersionId, parseVersionId)
                .orderByAsc(JdEvidenceBindingEntity::getCreatedAt)
                .orderByAsc(JdEvidenceBindingEntity::getId));
    }

    public long countByJobId(String jobId) {
        return mapper.selectCount(Wrappers.<JdEvidenceBindingEntity>lambdaQuery()
                .eq(JdEvidenceBindingEntity::getJobId, jobId));
    }
}
