<script setup lang="ts">
import { Bell, CheckCircle2, ChevronDown, ClipboardCheck, Search } from 'lucide-vue-next'

const props = defineProps<{
  searchQuery: string
  providerMode: string
  source: 'api' | 'fallback'
  statusLabel: string
  statusTone: 'draft' | 'ready'
  searchPlaceholder?: string
  reviewStatusLabel?: string
}>()

const emit = defineEmits<{
  'update:searchQuery': [value: string]
  'update:providerMode': [value: string]
}>()

const providers = ['local-rule', 'OpenAI-compatible', 'DeepSeek']
</script>

<template>
  <header class="topbar">
    <label class="search-box">
      <Search :size="17" />
      <input
        :value="props.searchQuery"
        type="search"
        :placeholder="props.searchPlaceholder ?? '搜索岗位、项目、技能或证据来源'"
        :aria-label="props.searchPlaceholder ?? '搜索岗位、项目、技能或证据来源'"
        @input="emit('update:searchQuery', ($event.target as HTMLInputElement).value)"
      />
      <kbd>⌘ K</kbd>
    </label>

    <div class="provider-switch" aria-label="Provider 模式">
      <span class="switch-label">Provider</span>
      <button
        v-for="item in providers"
        :key="item"
        type="button"
        :class="{ active: props.providerMode === item }"
        :aria-pressed="props.providerMode === item"
        @click="emit('update:providerMode', item)"
      >
        {{ item }}
      </button>
    </div>

    <div class="topbar-actions">
      <span class="source-pill" :class="props.source">
        <span class="pulse-dot" />
        {{ props.source === 'api' ? '本地 API' : '演示快照' }}
      </span>
      <span v-if="props.reviewStatusLabel" class="review-count-pill">
        <ClipboardCheck :size="13" />
        {{ props.reviewStatusLabel }}
      </span>
      <span class="page-status-pill" :class="props.statusTone">
        <CheckCircle2 v-if="props.statusTone === 'ready'" :size="13" />
        <span v-else />
        {{ props.statusLabel }}
      </span>
      <button class="icon-button" type="button" aria-label="通知">
        <Bell :size="18" />
      </button>
      <button class="avatar-button" type="button" aria-label="用户菜单">
        Y
        <ChevronDown :size="13" />
      </button>
    </div>
  </header>
</template>
