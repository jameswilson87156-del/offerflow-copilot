<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import { statusLabel } from '../utils/status'

interface AuditEventLike {
  id: string
  action: string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  actor: string
  actorRole: string
  humanNote: string
  createdAt: string
  changedFields?: string[]
  traceId?: string
  traceHash?: string
  previousRiskLevel?: string
  nextRiskLevel?: string
  copyAllowed?: boolean | null
  copyReason?: string | null
  versionStatus?: string | null
  humanReviewStatus?: string | null
  boundaryNotice?: string | null
}

const props = withDefaults(defineProps<{
  event: AuditEventLike
  tone?: string
  fieldLabels?: Record<string, string>
}>(), {
  tone: 'default',
  fieldLabels: () => ({}),
})

const expanded = shallowRef(false)
const detailId = computed(() => `audit-detail-${props.event.id}`)
const hasCopyResult = computed(() => props.event.copyAllowed !== undefined && props.event.copyAllowed !== null)
const stateFlow = computed(() => `${statusLabel(props.event.previousStatus || 'NONE')} → ${statusLabel(props.event.nextStatus)}`)

function fieldLabel(field: string) {
  return props.fieldLabels[field] ?? field
}
</script>

<template>
  <article class="audit-disclosure" :class="props.tone">
    <button
      type="button"
      class="audit-disclosure-summary"
      :aria-expanded="expanded"
      :aria-controls="detailId"
      @click="expanded = !expanded"
    >
      <span class="audit-disclosure-dot" aria-hidden="true" />
      <span class="audit-disclosure-title">
        <strong>{{ props.event.actionLabel }}</strong>
        <small>{{ stateFlow }}</small>
      </span>
      <time>{{ props.event.createdAt }}</time>
      <ChevronDown :size="13" :class="{ expanded }" aria-hidden="true" />
    </button>

    <div v-if="expanded" :id="detailId" class="audit-disclosure-detail" data-testid="audit-event-detail">
      <dl class="audit-detail-grid">
        <div><dt>Action</dt><dd>{{ props.event.action }}</dd></div>
        <div><dt>Previous Status</dt><dd>{{ statusLabel(props.event.previousStatus || 'NONE') }}</dd></div>
        <div><dt>Next Status</dt><dd>{{ statusLabel(props.event.nextStatus) }}</dd></div>
        <div><dt>Actor</dt><dd>{{ props.event.actor }}</dd></div>
        <div><dt>Actor Role</dt><dd>{{ props.event.actorRole }}</dd></div>
        <div><dt>Created At</dt><dd>{{ props.event.createdAt }}</dd></div>
        <div v-if="props.event.traceId"><dt>Trace ID</dt><dd>{{ props.event.traceId }}</dd></div>
        <div v-if="props.event.traceHash"><dt>Trace Hash</dt><dd>{{ props.event.traceHash }}</dd></div>
        <div v-if="props.event.previousRiskLevel || props.event.nextRiskLevel">
          <dt>Risk</dt><dd>{{ props.event.previousRiskLevel }} → {{ props.event.nextRiskLevel }}</dd>
        </div>
      </dl>

      <div v-if="props.event.changedFields?.length" class="audit-changed-fields">
        <strong>Changed Fields</strong>
        <span v-for="field in props.event.changedFields" :key="field">{{ fieldLabel(field) }}</span>
      </div>

      <blockquote>{{ props.event.humanNote || '无人工备注' }}</blockquote>

      <section v-if="hasCopyResult" class="audit-copy-result" :class="props.event.copyAllowed ? 'allowed' : 'blocked'">
        <strong>{{ props.event.copyAllowed ? '允许复制' : '禁止复制' }}</strong>
        <dl>
          <div><dt>阻止/许可原因</dt><dd>{{ props.event.copyReason }}</dd></div>
          <div><dt>Version Status</dt><dd>{{ statusLabel(props.event.versionStatus ?? '') }}</dd></div>
          <div><dt>Human Review</dt><dd>{{ statusLabel(props.event.humanReviewStatus ?? '') }}</dd></div>
          <div><dt>Boundary Notice</dt><dd>{{ props.event.boundaryNotice }}</dd></div>
        </dl>
      </section>
    </div>
  </article>
</template>
