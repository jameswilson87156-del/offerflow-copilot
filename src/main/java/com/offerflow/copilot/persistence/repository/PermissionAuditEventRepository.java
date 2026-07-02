package com.offerflow.copilot.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.offerflow.copilot.persistence.entity.PermissionAuditEventEntity;
import com.offerflow.copilot.persistence.mapper.PermissionAuditEventMapper;
import com.offerflow.copilot.security.local.PermissionAction;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionAuditEventRepository {

    private final PermissionAuditEventMapper mapper;

    public PermissionAuditEventRepository(PermissionAuditEventMapper mapper) {
        this.mapper = mapper;
    }

    public long count() {
        return mapper.selectCount(null);
    }

    public void save(PermissionAuditEventEntity entity) {
        mapper.insert(entity);
    }

    public List<PermissionAuditEventEntity> find(
            String actor,
            PermissionAction action,
            String targetType,
            String targetId,
            Boolean allowed) {
        LambdaQueryWrapper<PermissionAuditEventEntity> query = Wrappers.lambdaQuery();
        if (actor != null && !actor.isBlank()) {
            query.eq(PermissionAuditEventEntity::getActor, actor.trim());
        }
        if (action != null) {
            query.eq(PermissionAuditEventEntity::getAction, action.name());
        }
        if (targetType != null && !targetType.isBlank()) {
            query.eq(PermissionAuditEventEntity::getTargetType, targetType.trim());
        }
        if (targetId != null && !targetId.isBlank()) {
            query.eq(PermissionAuditEventEntity::getTargetId, targetId.trim());
        }
        if (allowed != null) {
            query.eq(PermissionAuditEventEntity::getAllowed, allowed);
        }
        query.orderByDesc(PermissionAuditEventEntity::getCreatedAt);
        return mapper.selectList(query);
    }
}
