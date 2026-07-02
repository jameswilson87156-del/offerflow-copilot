<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AppTopbar from './components/AppTopbar.vue'
import { useLocalActor } from './composables/useLocalActor'
import { useProviderStatus } from './composables/useProviderStatus'

const route = useRoute()
const searchQuery = shallowRef('')
const selectedProvider = shallowRef('local-rule')
const { source } = useProviderStatus()
const { actor, actorRole, localActorRoles, setActorRole } = useLocalActor()

const pageStatus = computed(() => {
  if (route.name === 'evidence-library') return { label: 'Evidence Ready', tone: 'ready' as const }
  if (route.name === 'human-review') return { label: 'Draft，需要人工确认', tone: 'draft' as const }
  if (route.name === 'provider-settings') return { label: 'local-rule fallback active', tone: 'draft' as const }
  if (route.name === 'application-tracker') return { label: 'Manual tracking only', tone: 'ready' as const }
  return { label: 'Draft · 需复核', tone: 'draft' as const }
})

const searchPlaceholder = computed(() => {
  if (route.name === 'human-review') return '搜索审核项、岗位、项目或风险'
  if (route.name === 'provider-settings') return '搜索 Run ID、Provider、Trace、模型或岗位'
  if (route.name === 'match-report') return '搜索岗位、项目、技能差距或推荐行动'
  if (route.name === 'interview-prep') return '搜索面试问题、项目、STAR 或风险'
  if (route.name === 'application-tracker') return '搜索公司、岗位、城市、简历版本或状态'
  return '搜索岗位、项目、技能或证据来源'
})

const reviewStatusLabel = computed(() => route.name === 'human-review' ? '12 个待复核' : undefined)
</script>

<template>
  <div class="app-shell">
    <AppSidebar />
    <div class="app-body">
      <AppTopbar
        v-model:search-query="searchQuery"
        :provider-mode="selectedProvider"
        :source="source"
        :status-label="pageStatus.label"
        :status-tone="pageStatus.tone"
        :search-placeholder="searchPlaceholder"
        :review-status-label="reviewStatusLabel"
        :actor="actor"
        :actor-role="actorRole"
        :actor-roles="localActorRoles"
        @update:provider-mode="selectedProvider = $event"
        @update:actor-role="setActorRole($event)"
      />
      <RouterView v-slot="{ Component }">
        <component
          :is="Component"
          :search-query="searchQuery"
          :selected-provider="selectedProvider"
        />
      </RouterView>
    </div>
  </div>
</template>
