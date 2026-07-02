import { computed, shallowRef } from 'vue'
import type { LocalActorContext, LocalActorRole, PermissionAction } from '../types'

export const localActorRoles: LocalActorRole[] = ['OWNER', 'REVIEWER', 'EDITOR', 'VIEWER']

const actorRole = shallowRef<LocalActorRole>('REVIEWER')

const rolePermissions: Record<LocalActorRole, PermissionAction[]> = {
  OWNER: [
    'REVIEW_CONFIRM',
    'REVIEW_RETURN',
    'REVIEW_FLAG_RISK',
    'EVIDENCE_CREATE',
    'EVIDENCE_UPDATE',
    'EVIDENCE_CONFIRM',
    'EVIDENCE_ARCHIVE',
    'EVIDENCE_RESTORE',
    'JD_CREATE',
    'JD_UPDATE',
    'JD_PARSE',
    'JD_BIND_EVIDENCE',
    'MATCH_REPORT_GENERATE',
    'MATCH_REPORT_SEND_TO_REVIEW',
    'MATCH_REPORT_ARCHIVE',
    'MATCH_REPORT_RESTORE',
    'COPY_CHECK',
    'PROVIDER_SANDBOX_RUN',
  ],
  REVIEWER: ['REVIEW_CONFIRM', 'REVIEW_RETURN', 'REVIEW_FLAG_RISK', 'COPY_CHECK'],
  EDITOR: [
    'EVIDENCE_CREATE',
    'EVIDENCE_UPDATE',
    'JD_CREATE',
    'JD_UPDATE',
    'JD_PARSE',
    'JD_BIND_EVIDENCE',
    'MATCH_REPORT_GENERATE',
    'MATCH_REPORT_SEND_TO_REVIEW',
  ],
  VIEWER: [],
  SYSTEM: ['JD_PARSE', 'JD_BIND_EVIDENCE', 'MATCH_REPORT_GENERATE', 'PROVIDER_SANDBOX_RUN'],
}

const actor = computed(() => `demo.${actorRole.value.toLowerCase()}`)

const actorContext = computed<LocalActorContext>(() => ({
  actor: actor.value,
  actorRole: actorRole.value,
}))

export function useLocalActor() {
  function setActorRole(nextRole: LocalActorRole) {
    if (localActorRoles.includes(nextRole)) actorRole.value = nextRole
  }

  function can(action: PermissionAction) {
    return rolePermissions[actorRole.value].includes(action)
  }

  function permissionReason(action: PermissionAction) {
    if (can(action)) return ''
    if (actorRole.value === 'VIEWER') return '当前角色只读，无法执行该操作'
    if (actorRole.value === 'REVIEWER') return 'REVIEWER 仅可执行人工复核与复制检查'
    if (actorRole.value === 'EDITOR') return 'EDITOR 不能执行最终确认、归档、恢复或复制确认内容'
    return `${actorRole.value} 无法执行 ${action}`
  }

  function withActor<T extends object>(payload: T): T & LocalActorContext {
    return {
      ...payload,
      actor: actor.value,
      actorRole: actorRole.value,
    }
  }

  return {
    actor,
    actorRole,
    actorContext,
    localActorRoles,
    setActorRole,
    can,
    permissionReason,
    withActor,
  }
}
