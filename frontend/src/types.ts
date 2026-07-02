export interface ProviderStatus {
  mode: string
  openaiCompatibleReady: boolean
  deepSeekReady: boolean
  realCallEnabled: boolean
  rawResponseSave: boolean
  fallback: string
  boundaryNotice: string
}

export type LocalActorRole = 'OWNER' | 'REVIEWER' | 'EDITOR' | 'VIEWER' | 'SYSTEM'

export type PermissionAction =
  | 'REVIEW_CONFIRM'
  | 'REVIEW_RETURN'
  | 'REVIEW_FLAG_RISK'
  | 'EVIDENCE_CREATE'
  | 'EVIDENCE_UPDATE'
  | 'EVIDENCE_CONFIRM'
  | 'EVIDENCE_ARCHIVE'
  | 'EVIDENCE_RESTORE'
  | 'JD_CREATE'
  | 'JD_UPDATE'
  | 'JD_PARSE'
  | 'JD_BIND_EVIDENCE'
  | 'MATCH_REPORT_GENERATE'
  | 'MATCH_REPORT_SEND_TO_REVIEW'
  | 'MATCH_REPORT_ARCHIVE'
  | 'MATCH_REPORT_RESTORE'
  | 'COPY_CHECK'
  | 'PROVIDER_SANDBOX_RUN'
  | 'PROVIDER_REAL_DRY_RUN'

export interface LocalActorContext {
  actor: string
  actorRole: LocalActorRole
}

export interface PermissionDecision {
  allowed: boolean
  reason: string
  actor: string
  actorRole: LocalActorRole
  action: PermissionAction
  targetType: string
  targetId: string
  boundaryNotice: string
}

export interface PermissionAuditEvent {
  id: string
  actor: string
  actorRole: LocalActorRole
  action: PermissionAction
  targetType: string
  targetId: string
  allowed: boolean
  reason: string
  boundaryNotice: string
  requestId: string
  createdAt: string
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

export type JobIntakeStatus = 'Draft' | 'Parsed' | 'Bound' | 'Review Required'

export interface JobSummary {
  id: string
  title: string
  company: string
  city: string
  sourceType: string
  sourceNote: string
  status: JobIntakeStatus
  currentVersion: number
  bindingCount: number
  updatedAt: string
}

export interface JobListData {
  mode: string
  items: JobSummary[]
  boundaryNotice: string
}

export interface JobPostDetail {
  id: string
  title: string
  company: string
  city: string
  jdText: string
  sourceType: string
  sourceNote: string
  sanitized: boolean
  status: JobIntakeStatus
  createdAt: string
  updatedAt: string
}

export interface JdParseVersion {
  id: string
  jobId: string
  versionNo: number
  parserMode: string
  providerMode: string
  promptVersion: string
  schemaVersion: string
  extractedRequirements: RequirementGroup[]
  keywords: string[]
  riskTerms: string[]
  sanitizedText: string
  parseStatus: string
  createdAt: string
}

export interface JdEvidenceBinding {
  id: string
  jobId: string
  parseVersionId: string
  requirementKey: string
  requirementLabel: string
  evidenceId: string
  evidenceProject: string
  evidenceStrength: '强' | '中' | '弱' | string
  bindingReason: string
  evidenceSource: string
  reviewStatus: string
  createdAt: string
  updatedAt: string
}

export interface JdSnapshot {
  title: string
  company: string
  city: string
  sourceType: string
  sourceNote: string
  status: string
  currentVersion: number
  bindingCount: number
  keywords: string[]
  riskTerms: string[]
}

export interface JdAuditEvent {
  id: string
  jobId: string
  action: 'CREATE_JD' | 'UPDATE_JD' | 'PARSE_LOCAL_RULE' | 'CREATE_PARSE_VERSION' | 'BIND_EVIDENCE' | 'REBIND_EVIDENCE' | 'ARCHIVE_JD' | 'RESTORE_JD' | string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  actor: string
  actorRole: string
  changedFields: string[]
  beforeSnapshot: JdSnapshot
  afterSnapshot: JdSnapshot
  humanNote: string
  createdAt: string
}

export interface JobDetailData {
  mode: string
  job: JobPostDetail
  currentParseVersion: JdParseVersion | null
  requirementGroups: RequirementGroup[]
  evidenceBindings: JdEvidenceBinding[]
  parseVersions: JdParseVersion[]
  auditTrail: JdAuditEvent[]
  boundaryNotice: string
}

export interface JobMutationPayload {
  title?: string
  company?: string
  city?: string
  jdText?: string
  sourceType?: string
  sourceNote?: string
  actor?: string
  actorRole?: string
  humanNote?: string
}

export interface EvidenceCategory {
  key: string
  label: string
  count: number
}

export interface EvidenceSourceStep {
  label: string
  status: 'verified' | 'partial' | 'missing'
  note: string
}

export interface EvidenceDetail {
  relatedSkills: string[]
  suitableRoles: string[]
  interviewAnswers: string[]
  riskBoundaries: string[]
  sourceChain: EvidenceSourceStep[]
}

export type EvidenceStatus = 'Draft' | 'Confirmed' | 'Returned' | 'Archived'

export interface EvidenceItem {
  id: string
  projectName: string
  projectSlug: string
  summary: string
  abilityTags: string[]
  evidenceSources: string[]
  credibility: '强' | '中' | '弱'
  matchableRequirements: string[]
  humanReviewStatus: 'Confirmed' | 'Needs review'
  status: EvidenceStatus
  updatedAt: string
  auditCount: number
  detail: EvidenceDetail
}

export interface EvidenceSnapshot {
  projectName: string | null
  summary: string | null
  skills: string[]
  abilityTags: string[]
  evidenceSources: string[]
  strength: string | null
  matchableRequirements: string[]
  boundaryNote: string | null
  riskBoundaries: string[]
}

export interface EvidenceAuditEvent {
  id: string
  evidenceId: string
  action: 'CREATE_DRAFT' | 'UPDATE_DRAFT' | 'CONFIRM' | 'RETURN_TO_DRAFT' | 'ARCHIVE' | 'RESTORE' | string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  actor: string
  actorRole: string
  changedFields: string[]
  beforeSnapshot: EvidenceSnapshot
  afterSnapshot: EvidenceSnapshot
  humanNote: string
  traceId?: string
  traceHash?: string
  createdAt: string
}

export interface EvidenceItemDetail {
  mode: string
  item: EvidenceItem
  auditTrail: EvidenceAuditEvent[]
  boundaryNotice: string
}

export interface EvidenceMutationPayload {
  actor?: string
  actorRole?: string
  humanNote?: string
  projectName?: string
  summary?: string
  abilityTags?: string[]
  evidenceSources?: string[]
  credibility?: string
  matchableRequirements?: string[]
  boundaryNote?: string
  relatedSkills?: string[]
  suitableRoles?: string[]
  interviewAnswers?: string[]
  riskBoundaries?: string[]
  targetStatus?: EvidenceStatus
}

export interface EvidenceLibraryData {
  mode: string
  total: number
  categories: EvidenceCategory[]
  items: EvidenceItem[]
  disclaimer: string
}

export interface CoverageItem {
  skill: string
  level: '强支撑' | '中支撑' | '弱支撑' | '暂无证据'
  score: number
  supportingProjects: string[]
  gap: string
}

export interface EvidenceCoverageData {
  mode: string
  items: CoverageItem[]
  note: string
}

export type HumanReviewStatus = 'Draft' | 'In Review' | 'Returned' | 'Confirmed' | 'Risk Flagged' | 'Archived'

export interface HumanReviewGroup {
  key: string
  label: string
  count: number
}

export interface HumanReviewSummary {
  id: string
  group: string
  title: string
  sourcePage: string
  riskLevel: string
  providerMode: string
  traceId: string
  status: HumanReviewStatus
  updatedAt: string
}

export interface HumanReviewProject {
  name: string
  excerpt: string
  sourceTypes: string[]
}

export interface HumanReviewEvidence {
  jdSnippet: string
  resumeProjects: HumanReviewProject[]
  evidenceNote: string
}

export interface HumanReviewTraceStep {
  label: string
  status: 'done' | 'warning' | 'current'
  detail: string
}

export interface HumanReviewAuditEvent {
  id: string
  reviewId: string
  action: 'CONFIRM' | 'RETURN' | 'FLAG_RISK' | 'ADD_NOTE' | 'AUTO_RISK_GUARD' | string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  previousRiskLevel: string
  nextRiskLevel: string
  actor: string
  actorRole: string
  changedFields?: string[]
  humanNote: string
  traceId: string
  traceHash: string
  createdAt: string
}

export interface HumanReviewDetail extends HumanReviewSummary {
  reviewer: string
  humanNote: string
  aiSuggestion: string
  evidence: HumanReviewEvidence
  riskTerms: string[]
  traceEvidence: HumanReviewTraceStep[]
  compliancePrinciples: string[]
  copyAllowed: boolean
  lastAction: string
  auditTrail: HumanReviewAuditEvent[]
}

export interface HumanReviewCenterData {
  mode: string
  pendingReviewCount: number
  groups: HumanReviewGroup[]
  items: HumanReviewSummary[]
  compliancePrinciples: string[]
}

export type ProviderTraceStepStatus = 'success' | 'fallback' | 'warning' | 'SUCCESS' | 'FALLBACK' | 'WARNING' | 'BLOCKED' | 'ERROR'

export interface ProviderCard {
  providerMode: string
  displayName: string
  baseUrlConfigured: boolean
  configured: boolean
  apiKeyStatus: string
  model: string
  timeoutMs: number
  active: boolean
  realCallEnabled: boolean
  rawResponseSave: boolean
  fallbackPolicy: string
  boundaryNotice: string
}

export interface SafetyBoundary {
  title: string
  description: string
  tone: string
}

export interface ProviderSettingsData {
  mode: string
  currentStatus: string
  providers: ProviderCard[]
  safetyBoundaries: SafetyBoundary[]
}

export interface ProviderConfigCheck {
  providerMode: string
  localRuleAvailable: boolean
  openAiCompatibleConfigured: boolean
  deepSeekConfigured: boolean
  realCallEnabled: boolean
  rawResponseSave: boolean
  apiKeyStatus: Record<string, string>
  warnings: string[]
  boundaryNotice: string
}

export interface ProviderSandboxRunPayload {
  taskType: string
  inputText: string
  providerMode: string
  simulateFailure: boolean
  simulateTimeout: boolean
  actor: string
  actorRole: string
}

export interface ProviderRealDryRunPayload {
  providerMode: string
  taskType: string
  inputText: string
  actor: string
  actorRole: string
  allowExternalCall: boolean
  confirmNoPii: boolean
}

export interface ProviderRealDryRunResult {
  success: boolean
  externalCallAttempted: boolean
  externalCallBlocked: boolean
  providerMode: string
  finalProvider: string
  model: string
  fallbackUsed: boolean
  fallbackReason: string
  schemaValidated: boolean
  riskGuardPassed: boolean
  humanReviewRequired: boolean
  copyAllowed: boolean
  rawResponseSaved: boolean
  traceId: string
  runId: string
  riskFlags: string[]
  boundaryNotice: string
}

export interface ProviderResponse {
  success: boolean
  providerMode: string
  finalProvider: string
  model: string
  outputText: string
  structuredJson: string
  fallbackUsed: boolean
  fallbackReason: string
  errorCode: string
  errorMessage: string
  durationMs: number
  traceId: string
  riskFlags: string[]
  rawResponseSaved: boolean
  humanReviewRequired: boolean
}

export interface ProviderTraceSummary {
  runId: string
  jobTitle: string
  providerMode: string
  finalProvider: string
  status: string
  startedAt: string
  duration: string
  evidenceCount: number
  humanReviewStatus: string
}

export interface ProviderTraceIndex {
  mode: string
  currentStatus: string
  items: ProviderTraceSummary[]
}

export interface ProviderPipelineStep {
  key: string
  label: string
  status: ProviderTraceStepStatus
  duration: string
  inputSummary: string
  outputSummary: string
  linkedEvidence: string
}

export interface ProviderResumeEvidenceRef {
  id: string
  title: string
  excerpt: string
  sources: string[]
}

export interface ProviderTraceEvidenceDetail {
  jdSnippet: string
  resumeEvidence: ProviderResumeEvidenceRef[]
  jsonSummary: string
  fallbackReason: string
  humanNote: string
}

export interface ProviderTraceRun {
  runId: string
  jobTitle: string
  providerMode: string
  finalProvider: string
  model: string
  fallbackReason: string
  promptVersion: string
  schemaVersion: string
  riskFlags: string[]
  evidenceCount: number
  humanReviewStatus: string
  duration: string
  traceHash: string
  pipeline: ProviderPipelineStep[]
  evidenceDetail: ProviderTraceEvidenceDetail
  technicalTags: string[]
}

export interface ProviderContractSummary {
  taskType: string
  displayName: string
  promptVersion: string
  schemaVersion: string
  riskPolicyVersion: string
  requireHumanReview: boolean
  outputSchemaName: string
  boundaryNotice: string
}

export interface ProviderPromptContract {
  taskType: string
  promptVersion: string
  schemaVersion: string
  riskPolicyVersion: string
  systemInstruction: string
  userInstructionTemplate: string
  requiredInputs: string[]
  forbiddenClaims: string[]
  outputSchemaName: string
  boundaryNotice: string
}

export interface ProviderContractViolation {
  code: string
  message: string
  field: string
  severity: string
  fallbackRequired: boolean
  humanReviewRequired: boolean
}

export interface ProviderValidatedResult {
  valid: boolean
  violations: ProviderContractViolation[]
  sanitizedOutput: string
  fallbackRequired: boolean
  humanReviewRequired: boolean
  riskFlags: string[]
  schemaVersion: string
  promptVersion: string
  riskPolicyVersion: string
}

export interface ProviderValidationPayload {
  taskType: string
  providerMode: string
  model: string
  structuredJson?: string
  outputText?: string
  simulateUnsafeClaim: boolean
  simulateMissingField: boolean
  simulateSchemaMismatch: boolean
}

export type CopyTargetType =
  | 'MATCH_REPORT'
  | 'INTERVIEW_PREP'
  | 'OPENING_MESSAGE'
  | 'HUMAN_REVIEW_REWRITE'
  | 'JD_ANALYSIS_SUMMARY'
  | 'EVIDENCE_BINDING_SUMMARY'
  | 'PROVIDER_SANDBOX_OUTPUT'

export interface CopyPermissionResult {
  allowed: boolean
  reason: string
  targetType: CopyTargetType
  targetId: string
  targetStatus: string
  humanReviewStatus: string
  schemaValidated: boolean
  riskGuardPassed: boolean
  confirmed: boolean
  boundaryNotice: string
  auditEventId: string
  copyText: string
}

export interface CopyPermissionAuditEvent {
  id: string
  targetType: CopyTargetType
  targetId: string
  action: 'COPY_CHECK' | 'COPY_ALLOWED' | 'COPY_BLOCKED' | string
  allowed: boolean
  reason: string
  targetStatus: string
  humanReviewStatus: string
  schemaValidated: boolean
  riskGuardPassed: boolean
  actor: string
  actorRole: string
  traceId: string
  providerRunId: string
  boundaryNotice: string
  createdAt: string
}

export interface MatchReportSummary {
  jobTitle: string
  recommendedResumeVersions: string[]
  totalScore: number
  maximumScore: number
  status: string
  note: string
}

export interface MatchReportScoreItem {
  key: string
  label: string
  value: number
  maximum: number
  detail: string
  tone: string
}

export interface MatchReportEvidenceSource {
  requirement: string
  project: string
  strength: '强' | '中' | '弱'
  evidenceTypes: string[]
  rationale: string
}

export interface MatchReportSkillGap {
  skill: string
  severity: string
  reason: string
  nextAction: string
}

export interface MatchReportAction {
  title: string
  detail: string
  priority: string
}

export interface WorkflowTraceStep {
  label: string
  status: 'success' | 'warning' | 'current' | 'upcoming'
  detail: string
}

export type MatchReportVersionStatus = 'DRAFT' | 'IN_REVIEW' | 'CONFIRMED' | 'RETURNED' | 'RISK_FLAGGED' | 'ARCHIVED'

export interface MatchReportCopyCheck {
  allowed: boolean
  reason: string
  versionStatus: MatchReportVersionStatus
  humanReviewStatus: string
  boundaryNotice: string
  targetType?: CopyTargetType
  targetId?: string
  schemaValidated?: boolean
  riskGuardPassed?: boolean
  confirmed?: boolean
  auditEventId?: string
}

export interface MatchReportVersionSummary {
  id: string
  reportId: string
  jobId: string
  parseVersionId: string
  parseVersionNo: number
  versionNo: number
  score: number
  status: MatchReportVersionStatus
  providerMode: string
  promptVersion: string
  traceId: string
  humanReviewId: string
  humanReviewStatus: string
  evidenceBindingCount: number
  createdAt: string
}

export interface MatchReportAuditEvent {
  id: string
  reportVersionId: string
  action: string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  actor: string
  actorRole: string
  changedFields: string[]
  humanNote: string
  traceId: string
  copyAllowed?: boolean | null
  copyReason?: string | null
  versionStatus?: MatchReportVersionStatus | null
  humanReviewStatus?: string | null
  boundaryNotice?: string | null
  createdAt: string
}

export interface MatchReportData {
  mode: string
  reportId: string
  versionId: string
  jobId: string
  versionNo: number
  status: MatchReportVersionStatus
  createdAt: string
  updatedAt: string
  generatedBy: string
  providerMode: string
  promptVersion: string
  schemaVersion: string
  traceId: string
  parseVersionId: string
  parseVersionNo: number
  evidenceBindingCount: number
  humanReviewId: string
  humanReviewStatus: string
  summary: MatchReportSummary
  score: {
    items: MatchReportScoreItem[]
    note: string
  }
  evidenceSources: MatchReportEvidenceSource[]
  skillGaps: MatchReportSkillGap[]
  recommendedActions: MatchReportAction[]
  riskNotes: string[]
  traceEvidence: WorkflowTraceStep[]
  disclaimer: string
}

export interface InterviewFocusArea {
  label: string
  detail: string
  evidence: string
}

export interface InterviewQuestionGroup {
  key: string
  label: string
  questions: string[]
}

export interface InterviewStarDraft {
  situation: string
  task: string
  action: string
  result: string
  riskNote: string
}

export interface InterviewTimelineStep {
  key: string
  label: string
  status: 'current' | 'upcoming' | 'done'
  detail: string
}

export interface InterviewPrepData {
  mode: string
  id: string
  jobTitle: string
  positioningNotice: string
  focusAreas: InterviewFocusArea[]
  questionGroups: InterviewQuestionGroup[]
  starDraft: InterviewStarDraft
  riskReminders: string[]
  reviewTimeline: InterviewTimelineStep[]
  reviewStatus: string
  disclaimer: string
}

export interface ApplicationBoardColumn {
  key: string
  label: string
  count: number
  tone: string
}

export interface ApplicationRecord {
  id: string
  company: string
  role: string
  city: string
  resumeVersion: string
  sourceNote: string
  status: string
  updatedAt: string
  nextAction: string
}

export interface CommunicationLog {
  applicationId: string
  stage: string
  note: string
  timestamp: string
}

export interface ApplicationTrackerData {
  mode: string
  boardColumns: ApplicationBoardColumn[]
  applications: ApplicationRecord[]
  communicationLogs: CommunicationLog[]
  riskBoundaries: string[]
  disclaimer: string
}
