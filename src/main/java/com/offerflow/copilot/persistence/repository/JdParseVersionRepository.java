package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.JdParseVersionEntity;
import com.offerflow.copilot.persistence.mapper.JdParseVersionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdParseVersionRepository {

    private final JdParseVersionMapper mapper;

    public JdParseVersionRepository(JdParseVersionMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(JdParseVersionEntity entity) {
        mapper.insert(entity);
    }

    public Optional<JdParseVersionEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<JdParseVersionEntity> findByJobId(String jobId) {
        return mapper.selectList(Wrappers.<JdParseVersionEntity>lambdaQuery()
                .eq(JdParseVersionEntity::getJobId, jobId)
                .orderByAsc(JdParseVersionEntity::getVersionNo));
    }

    public Optional<JdParseVersionEntity> findLatestByJobId(String jobId) {
        return mapper.selectList(Wrappers.<JdParseVersionEntity>lambdaQuery()
                        .eq(JdParseVersionEntity::getJobId, jobId)
                        .orderByDesc(JdParseVersionEntity::getVersionNo)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }

    public int nextVersionNo(String jobId) {
        return findLatestByJobId(jobId).map(version -> version.getVersionNo() + 1).orElse(1);
    }
}
