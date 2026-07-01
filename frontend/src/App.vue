<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AppTopbar from './components/AppTopbar.vue'
import { useProviderStatus } from './composables/useProviderStatus'

const route = useRoute()
const searchQuery = shallowRef('')
const selectedProvider = shallowRef('local-rule')
const { source } = useProviderStatus()

const pageStatus = computed(() => {
  if (route.name === 'evidence-library') return { label: 'Evidence Ready', tone: 'ready' as const }
  if (route.name === 'human-review') return { label: 'Draft，需要人工确认', tone: 'draft' as const }
  return { label: 'Draft · 需复核', tone: 'draft' as const }
})

const searchPlaceholder = computed(() => route.name === 'human-review'
  ? '搜索审核项、岗位、项目或风险'
  : '搜索岗位、项目、技能或证据来源')

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
        @update:provider-mode="selectedProvider = $event"
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
