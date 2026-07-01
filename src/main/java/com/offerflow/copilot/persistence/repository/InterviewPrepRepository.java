package com.offerflow.copilot.persistence.repository;

import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.InterviewPrepEntity;
import com.offerflow.copilot.persistence.mapper.InterviewPrepMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InterviewPrepRepository {

    private final InterviewPrepMapper mapper;

    public InterviewPrepRepository(InterviewPrepMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(InterviewPrepEntity entity) {
        mapper.insert(entity);
    }

    public Optional<InterviewPrepEntity> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.selectById(id.trim()));
    }

    public Optional<InterviewPrepEntity> findDemo() {
        return mapper.selectList(Wrappers.<InterviewPrepEntity>lambdaQuery()
                        .orderByAsc(InterviewPrepEntity::getCreatedAt)
                        .last("LIMIT 1"))
                .stream()
                .findFirst();
    }
}
