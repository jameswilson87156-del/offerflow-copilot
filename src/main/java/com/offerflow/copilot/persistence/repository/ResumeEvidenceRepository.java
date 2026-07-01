package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.mapper.ResumeEvidenceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ResumeEvidenceRepository {

    private final ResumeEvidenceMapper mapper;

    public ResumeEvidenceRepository(ResumeEvidenceMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(ResumeEvidenceEntity entity) {
        mapper.insert(entity);
    }

    public void update(ResumeEvidenceEntity entity) {
        mapper.updateById(entity);
    }

    public List<ResumeEvidenceEntity> findAll() {
        return mapper.selectList(Wrappers.<ResumeEvidenceEntity>lambdaQuery()
                .orderByAsc(ResumeEvidenceEntity::getCreatedAt));
    }

    public Optional<ResumeEvidenceEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
}
