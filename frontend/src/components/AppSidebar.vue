<script setup lang="ts">
import {
  BarChart3,
  BriefcaseBusiness,
  ClipboardCheck,
  FileSearch,
  FolderKanban,
  GitBranch,
  LayoutDashboard,
  MessageSquareText,
  Settings2,
  Sparkles,
} from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

const navigation = [
  { label: '总览', icon: LayoutDashboard },
  { label: 'JD 分析台', icon: FileSearch, to: '/jd-analyzer' },
  { label: '简历证据库', icon: FolderKanban, to: '/evidence-library' },
  { label: '匹配报告', icon: BarChart3 },
  { label: '面试准备', icon: MessageSquareText },
  { label: '投递跟踪', icon: BriefcaseBusiness },
  { label: '证据链', icon: GitBranch },
  { label: '人工复核', icon: ClipboardCheck, count: 2 },
]
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <span class="brand-mark" aria-hidden="true"><Sparkles :size="19" /></span>
      <span class="brand-copy">
        <strong>OfferFlow</strong>
        <small>Copilot</small>
      </span>
    </div>

    <nav aria-label="主导航" class="sidebar-nav">
      <p class="nav-eyebrow">WORKSPACE</p>
      <template v-for="item in navigation" :key="item.label">
        <RouterLink
          v-if="item.to"
          :to="item.to"
          class="nav-item"
          active-class="active"
          :title="item.label"
        >
          <component :is="item.icon" :size="18" :stroke-width="1.8" />
          <span>{{ item.label }}</span>
        </RouterLink>
        <button
          v-else
          class="nav-item pending-nav"
          type="button"
          disabled
          :title="`${item.label}将在后续阶段开放`"
        >
          <component :is="item.icon" :size="18" :stroke-width="1.8" />
          <span>{{ item.label }}</span>
          <em v-if="item.count">{{ item.count }}</em>
        </button>
      </template>
    </nav>

    <div class="sidebar-footer">
      <button class="nav-item pending-nav" type="button" title="Provider 设置将在后续阶段开放" disabled>
        <Settings2 :size="18" :stroke-width="1.8" />
        <span>Provider 设置</span>
      </button>
      <div class="local-boundary">
        <span class="pulse-dot" />
        <div>
          <strong>Local sandbox</strong>
          <small>无外部调用</small>
        </div>
      </div>
    </div>
  </aside>
</template>
