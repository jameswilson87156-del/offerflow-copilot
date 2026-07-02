<script setup lang="ts">
import {
  AlertTriangle,
  Archive,
  BadgeCheck,
  BriefcaseBusiness,
  Check,
  CircleDot,
  History,
  Link2,
  MessageSquareText,
  Pencil,
  RotateCcw,
  ShieldCheck,
  Undo2,
} from 'lucide-vue-next'
import type { EvidenceAuditEvent, EvidenceItem } from '../../types'
import AuditEventDisclosure from '../AuditEventDisclosure.vue'
import { statusClass, statusLabel } from '../../utils/status'

const props = defineProps<{
  item: EvidenceItem
  auditTrail: EvidenceAuditEvent[]
  boundaryNotice: string
  busy: boolean
  canEdit: boolean
  canConfirm: boolean
  canReturnToDraft: boolean
  canArchive: boolean
  canRestore: boolean
  editReason: string
  confirmReason: string
  returnReason: string
  archiveReason: string
  restoreReason: string
}>()

const emit = defineEmits<{
  edit: []
  confirm: []
  returnToDraft: []
  archive: []
  restore: []
}>()

const fieldLabels: Record<string, string> = {
  projectName: '项目名称',
  summary: '项目摘要',
  skills: '能力标签',
  abilityTags: '能力分类',
  evidenceSources: '证据来源',
  strength: '可信度',
  matchableRequirements: '可匹配要求',
  boundaryNote: '风险边界',
  riskBoundaries: '风险提醒',
  status: '状态',
}
</script>

<template>
  <aside class="panel evidence-detail" :class="{ 'readonly-state': props.item.status === 'Archived' }" aria-labelledby="detail-title">
    <header class="panel-header evidence-panel-header">
      <div>
        <span class="panel-kicker">EVIDENCE DETAIL</span>
        <h2 id="detail-title">Evidence Detail</h2>
      </div>
      <span class="status-chip detail-status" :class="statusClass(props.item.status)">
        {{ statusLabel(props.item.status) }}
      </span>
    </header>

    <div v-if="props.item.status === 'Archived'" class="evidence-readonly-note" role="status">
      <Archive :size="14" /><strong>已归档 · 只读</strong><span>不可编辑或确认，仅允许恢复为草稿。</span>
    </div>

    <div class="detail-project">
      <span><ShieldCheck :size="20" /></span>
      <div><strong>{{ props.item.projectName }}</strong><small>{{ props.item.summary }}</small></div>
    </div>

    <section class="evidence-actions">
      <button type="button" class="evidence-action-button primary" :disabled="props.busy || props.item.status === 'Confirmed' || props.item.status === 'Archived' || !props.canConfirm" :title="props.canConfirm ? '' : props.confirmReason" @click="emit('confirm')">
        <BadgeCheck :size="14" />确认证据
      </button>
      <button type="button" class="evidence-action-button" :disabled="props.busy || props.item.status === 'Archived' || !props.canEdit" :title="props.canEdit ? '' : props.editReason" @click="emit('edit')">
        <Pencil :size="14" />编辑
      </button>
      <button type="button" class="evidence-action-button" :disabled="props.busy || props.item.status === 'Draft' || props.item.status === 'Archived' || !props.canReturnToDraft" :title="props.canReturnToDraft ? '' : props.returnReason" @click="emit('returnToDraft')">
        <Undo2 :size="14" />退回草稿
      </button>
      <button
        v-if="props.item.status !== 'Archived'"
        type="button"
        class="evidence-action-button danger"
        :disabled="props.busy || !props.canArchive"
        :title="props.canArchive ? '' : props.archiveReason"
        @click="emit('archive')"
      >
        <Archive :size="14" />归档
      </button>
      <button v-else type="button" class="evidence-action-button primary" :disabled="props.busy || !props.canRestore" :title="props.canRestore ? '' : props.restoreReason" @click="emit('restore')">
        <RotateCcw :size="14" />恢复
      </button>
    </section>
    <p v-if="!props.canEdit || !props.canConfirm || !props.canArchive" class="permission-inline-note">
      {{ !props.canEdit ? props.editReason : !props.canConfirm ? props.confirmReason : props.archiveReason }}
    </p>

    <section class="detail-block skill-block">
      <h3><CircleDot :size="14" />关联技能</h3>
      <div class="detail-chips">
        <span v-for="skill in props.item.detail.relatedSkills" :key="skill">{{ skill }}</span>
      </div>
    </section>

    <section class="detail-block">
      <h3><BriefcaseBusiness :size="14" />可用于哪些岗位</h3>
      <ul class="role-list">
        <li v-for="role in props.item.detail.suitableRoles" :key="role"><Check :size="12" />{{ role }}</li>
      </ul>
    </section>

    <section class="detail-block">
      <h3><MessageSquareText :size="14" />可支撑哪些面试回答</h3>
      <ol class="answer-list">
        <li v-for="answer in props.item.detail.interviewAnswers" :key="answer">{{ answer }}</li>
      </ol>
    </section>

    <section class="detail-block detail-risk">
      <h3><AlertTriangle :size="14" />风险边界</h3>
      <ul>
        <li v-for="risk in props.item.detail.riskBoundaries" :key="risk">{{ risk }}</li>
      </ul>
    </section>

    <section class="source-chain">
      <h3><Link2 :size="14" />证据来源链路</h3>
      <div class="source-steps">
        <div v-for="(step, index) in props.item.detail.sourceChain" :key="step.label" class="source-step" :class="step.status">
          <span class="step-dot"><BadgeCheck v-if="step.status === 'verified'" :size="12" /><span v-else /></span>
          <div><strong>{{ step.label }}</strong><small>{{ step.note }}</small></div>
          <i v-if="index < props.item.detail.sourceChain.length - 1" />
        </div>
      </div>
    </section>

    <section class="evidence-audit-panel">
      <h3><History :size="14" />Evidence Audit Trail</h3>
      <p>{{ props.boundaryNotice }}</p>
      <div class="evidence-audit-list">
        <AuditEventDisclosure
          v-for="event in props.auditTrail"
          :key="event.id"
          :event="event"
          tone="evidence"
          :field-labels="fieldLabels"
        />
      </div>
    </section>
  </aside>
</template>
