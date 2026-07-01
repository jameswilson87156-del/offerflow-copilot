package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.HumanReviewAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.HumanReviewAuditEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HumanReviewAuditEventRepository {

    private final HumanReviewAuditEventMapper mapper;

    public HumanReviewAuditEventRepository(HumanReviewAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(HumanReviewAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<HumanReviewAuditEventEntity> findByReviewId(String reviewId) {
        return mapper.selectList(Wrappers.<HumanReviewAuditEventEntity>lambdaQuery()
                .eq(HumanReviewAuditEventEntity::getReviewId, reviewId)
                .orderByAsc(HumanReviewAuditEventEntity::getCreatedAt));
    }
}
