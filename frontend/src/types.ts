export interface ProviderStatus {
  mode: string
  openaiCompatibleReady: boolean
  deepSeekReady: boolean
  realCallEnabled: boolean
  fallback: string
}

export interface Requirement {
  id: string
  title: string
  description: string
  level: string
  keywords: string[]
}

export interface RequirementGroup {
  key: 'core' | 'bonus' | 'risk'
  label: string
  tone: string
  items: Requirement[]
}

export interface EvidenceMatch {
  requirement: string
  requirementDetail: string
  project: string
  projectSlug: string
  strength: '强' | '中' | '弱'
  rationale: string
  evidenceTypes: string[]
}

export interface ScoreItem {
  key: string
  label: string
  value: number
  maximum: number
  description: string
  tone: string
}

export interface DemoAnalysis {
  job: { title: string; company: string; source: string; updatedAt: string }
  requirementGroups: RequirementGroup[]
  evidenceMatches: EvidenceMatch[]
  score: { total: number; maximum: number; items: ScoreItem[]; note: string }
  interviewPreparation: {
    followUpQuestions: string[]
    starDraft: { situation: string; task: string; action: string; result: string }
    riskReminders: string[]
  }
  humanReview: {
    aiOutputStatus: string
    humanStatus: string
    copyAllowed: boolean
    instruction: string
  }
  timeline: Array<{ key: string; label: string; status: string; detail: string; timestamp: string }>
  recommendedResumes: Array<{ name: string; focus: string; recommended: boolean }>
  technicalTags: string[]
  disclaimer: string
}
