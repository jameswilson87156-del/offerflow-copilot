package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.ApplicationRecordEntity;
import com.offerflow.copilot.persistence.mapper.ApplicationRecordMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationRecordRepository {

    private final ApplicationRecordMapper mapper;

    public ApplicationRecordRepository(ApplicationRecordMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(ApplicationRecordEntity entity) {
        mapper.insert(entity);
    }

    public List<ApplicationRecordEntity> findAll() {
        return mapper.selectList(Wrappers.<ApplicationRecordEntity>lambdaQuery()
                .orderByDesc(ApplicationRecordEntity::getUpdatedAt));
    }
}
