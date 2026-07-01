package com.offerflow.copilot.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.mapper.HumanReviewItemMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HumanReviewItemRepository {

    private final HumanReviewItemMapper mapper;

    public HumanReviewItemRepository(HumanReviewItemMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(HumanReviewItemEntity entity) {
        mapper.insert(entity);
    }

    public void update(HumanReviewItemEntity entity) {
        mapper.updateById(entity);
    }

    public List<HumanReviewItemEntity> findAll() {
        return mapper.selectList(Wrappers.<HumanReviewItemEntity>lambdaQuery()
                .orderByAsc(HumanReviewItemEntity::getCreatedAt));
    }

    public Optional<HumanReviewItemEntity> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
}
