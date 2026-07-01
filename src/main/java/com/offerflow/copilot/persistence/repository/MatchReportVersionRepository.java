package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.MatchReportVersionEntity;
import com.offerflow.copilot.persistence.mapper.MatchReportVersionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MatchReportVersionRepository {

    private final MatchReportVersionMapper mapper;

    public MatchReportVersionRepository(MatchReportVersionMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(MatchReportVersionEntity entity) {
        mapper.insert(entity);
    }

    public void update(MatchReportVersionEntity entity) {
        mapper.updateById(entity);
    }

    public Optional<MatchReportVersionEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<MatchReportVersionEntity> findByJobId(String jobId) {
        return mapper.selectList(Wrappers.<MatchReportVersionEntity>lambdaQuery()
                .eq(MatchReportVersionEntity::getJobId, jobId)
                .orderByAsc(MatchReportVersionEntity::getVersionNo));
    }

    public Optional<MatchReportVersionEntity> findLatestByJobId(String jobId) {
        return mapper.selectList(Wrappers.<MatchReportVersionEntity>lambdaQuery()
                        .eq(MatchReportVersionEntity::getJobId, jobId)
                        .orderByDesc(MatchReportVersionEntity::getVersionNo)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }

    public Optional<MatchReportVersionEntity> findLatest() {
        return mapper.selectList(Wrappers.<MatchReportVersionEntity>lambdaQuery()
                        .orderByDesc(MatchReportVersionEntity::getCreatedAt)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }

    public int nextVersionNo(String jobId) {
        return findLatestByJobId(jobId).map(version -> version.getVersionNo() + 1).orElse(1);
    }
}
