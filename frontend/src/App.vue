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

const pageStatus = computed(() => route.name === 'evidence-library'
  ? { label: 'Evidence Ready', tone: 'ready' as const }
  : { label: 'Draft · 需复核', tone: 'draft' as const })
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
