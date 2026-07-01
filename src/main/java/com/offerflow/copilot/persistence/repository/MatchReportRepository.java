package com.offerflow.copilot.persistence.repository;

import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.MatchReportEntity;
import com.offerflow.copilot.persistence.mapper.MatchReportMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MatchReportRepository {

    private final MatchReportMapper mapper;

    public MatchReportRepository(MatchReportMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(MatchReportEntity entity) {
        mapper.insert(entity);
    }

    public Optional<MatchReportEntity> findDemo() {
        return mapper.selectList(Wrappers.<MatchReportEntity>lambdaQuery()
                        .orderByAsc(MatchReportEntity::getCreatedAt)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }
}
