package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.JobPostEntity;
import com.offerflow.copilot.persistence.mapper.JobPostMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JobPostRepository {

    private final JobPostMapper mapper;

    public JobPostRepository(JobPostMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(JobPostEntity entity) {
        mapper.insert(entity);
    }

    public void update(JobPostEntity entity) {
        mapper.updateById(entity);
    }

    public List<JobPostEntity> findAll() {
        return mapper.selectList(Wrappers.<JobPostEntity>lambdaQuery()
                .orderByDesc(JobPostEntity::getUpdatedAt));
    }

    public Optional<JobPostEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
}
