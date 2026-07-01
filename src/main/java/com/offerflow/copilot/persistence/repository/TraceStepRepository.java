package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.TraceStepEntity;
import com.offerflow.copilot.persistence.mapper.TraceStepMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TraceStepRepository {

    private final TraceStepMapper mapper;

    public TraceStepRepository(TraceStepMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(TraceStepEntity entity) {
        mapper.insert(entity);
    }

    public List<TraceStepEntity> findByRunId(String runId) {
        return mapper.selectList(Wrappers.<TraceStepEntity>lambdaQuery()
                .eq(TraceStepEntity::getRunId, runId)
                .orderByAsc(TraceStepEntity::getStepOrder));
    }
}
