package com.offerflow.copilot.persistence.repository;

import java.util.Optional;

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

    public Optional<JobPostEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
}
