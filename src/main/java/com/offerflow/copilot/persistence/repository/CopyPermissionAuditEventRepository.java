package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.copy.CopyTargetType;
import com.offerflow.copilot.persistence.entity.CopyPermissionAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.CopyPermissionAuditEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CopyPermissionAuditEventRepository {

    private final CopyPermissionAuditEventMapper mapper;

    public CopyPermissionAuditEventRepository(CopyPermissionAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(CopyPermissionAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<CopyPermissionAuditEventEntity> findByTarget(CopyTargetType targetType, String targetId) {
        LambdaQueryWrapper<CopyPermissionAuditEventEntity> query = Wrappers.lambdaQuery();
        if (targetType != null) {
            query.eq(CopyPermissionAuditEventEntity::getTargetType, targetType.name());
        }
        if (targetId != null && !targetId.isBlank()) {
            query.eq(CopyPermissionAuditEventEntity::getTargetId, targetId.trim());
        }
        query.orderByAsc(CopyPermissionAuditEventEntity::getCreatedAt);
        return mapper.selectList(query);
    }
}
