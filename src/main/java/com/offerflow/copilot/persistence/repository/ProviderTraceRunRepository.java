package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.ProviderTraceRunEntity;
import com.offerflow.copilot.persistence.mapper.ProviderTraceRunMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderTraceRunRepository {

    private final ProviderTraceRunMapper mapper;

    public ProviderTraceRunRepository(ProviderTraceRunMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(ProviderTraceRunEntity entity) {
        mapper.insert(entity);
    }

    public List<ProviderTraceRunEntity> findAll() {
        return mapper.selectList(Wrappers.<ProviderTraceRunEntity>lambdaQuery()
                .orderByDesc(ProviderTraceRunEntity::getCreatedAt));
    }

    public Optional<ProviderTraceRunEntity> findByRunId(String runId) {
        return mapper.selectList(Wrappers.<ProviderTraceRunEntity>lambdaQuery()
                        .eq(ProviderTraceRunEntity::getRunId, runId)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }
}
