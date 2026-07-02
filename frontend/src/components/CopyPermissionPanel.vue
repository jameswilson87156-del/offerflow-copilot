<script setup lang="ts">
import { computed } from 'vue'
import { AlertTriangle, ClipboardCheck, Copy, History, ShieldCheck } from 'lucide-vue-next'
import type { CopyPermissionAuditEvent, CopyPermissionResult } from '../types'

const props = defineProps<{
  title: string
  kicker: string
  description: string
  result: CopyPermissionResult
  auditEvents: CopyPermissionAuditEvent[]
  busy?: boolean
  copyLabel?: string
  checkDisabled?: boolean
  copyDisabled?: boolean
  disabledReason?: string
}>()

const emit = defineEmits<{
  check: []
  copy: []
}>()

const gateRows = computed(() => [
  ['Target status', props.result.targetStatus],
  ['Human Review', props.result.humanReviewStatus],
  ['Schema Validate', props.result.schemaValidated ? 'passed' : 'blocked'],
  ['Risk Guard', props.result.riskGuardPassed ? 'passed' : 'blocked'],
])

const recentEvents = computed(() =>
  [...props.auditEvents].slice(-4).reverse(),
)
</script>

<template>
  <div class="copy-contract-panel" :class="result.allowed ? 'allowed' : 'blocked'">
    <header class="copy-contract-header">
      <div>
        <span class="panel-kicker">{{ kicker }}</span>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <span class="copy-contract-state">
        <ShieldCheck v-if="result.allowed" :size="15" />
        <AlertTriangle v-else :size="15" />
        {{ result.allowed ? '允许复制' : '禁止复制' }}
      </span>
    </header>

    <div class="copy-contract-grid">
      <article class="copy-contract-decision">
        <ClipboardCheck :size="16" />
        <span>Decision</span>
        <strong>{{ result.allowed ? 'COPY_ALLOWED' : 'COPY_BLOCKED' }}</strong>
        <small>{{ result.reason }}</small>
      </article>
      <article v-for="[label, value] in gateRows" :key="label">
        <span>{{ label }}</span>
        <strong>{{ value }}</strong>
      </article>
    </div>

    <div class="copy-contract-actions">
      <button type="button" class="copy-action-button check" :disabled="busy || checkDisabled" :title="checkDisabled ? disabledReason : ''" @click="emit('check')">
        <ClipboardCheck :size="15" />检查复制许可
      </button>
      <button v-if="result.allowed" type="button" class="copy-action-button copy" :disabled="busy || copyDisabled" :title="copyDisabled ? disabledReason : ''" @click="emit('copy')">
        <Copy :size="15" />{{ copyLabel ?? '复制确认内容' }}
      </button>
    </div>
    <p v-if="checkDisabled || copyDisabled" class="permission-inline-note">{{ disabledReason }}</p>

    <p class="copy-contract-notice">{{ result.boundaryNotice }}</p>

    <section class="copy-contract-audit" aria-label="复制许可审计历史">
      <header>
        <History :size="14" />
        <strong>复制审计历史</strong>
        <em>{{ auditEvents.length }} 条</em>
      </header>
      <article v-for="event in recentEvents" :key="event.id" :class="event.allowed ? 'allowed' : 'blocked'">
        <div>
          <strong>{{ event.action }}</strong>
          <span>{{ event.createdAt }} · {{ event.actorRole }}</span>
        </div>
        <p>{{ event.reason }} · {{ event.targetStatus }} / {{ event.humanReviewStatus }}</p>
      </article>
      <p v-if="recentEvents.length === 0" class="copy-contract-empty">暂无统一复制门禁审计，执行检查后会写入 copy_permission_audit_event。</p>
    </section>
  </div>
</template>
