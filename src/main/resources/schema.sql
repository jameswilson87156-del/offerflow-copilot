CREATE TABLE IF NOT EXISTS resume_evidence (
    id VARCHAR(80) PRIMARY KEY,
    project_name VARCHAR(160) NOT NULL,
    project_slug VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    summary TEXT NOT NULL,
    skills_json TEXT NOT NULL,
    ability_tags_json TEXT NOT NULL,
    evidence_sources_json TEXT NOT NULL,
    matchable_requirements_json TEXT NOT NULL,
    detail_json TEXT NOT NULL,
    strength VARCHAR(40) NOT NULL,
    review_status VARCHAR(40) NOT NULL,
    boundary_note TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS resume_evidence_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    evidence_id VARCHAR(80) NOT NULL,
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(40) NOT NULL,
    next_status VARCHAR(40) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    changed_fields_json TEXT NOT NULL,
    before_snapshot_json TEXT NOT NULL,
    after_snapshot_json TEXT NOT NULL,
    human_note TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS job_post (
    id VARCHAR(80) PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    company VARCHAR(160) NOT NULL,
    city VARCHAR(80) NOT NULL,
    jd_text TEXT NOT NULL,
    source_type VARCHAR(80) NOT NULL,
    source_note TEXT NOT NULL,
    sanitized BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS jd_parse_version (
    id VARCHAR(80) PRIMARY KEY,
    job_id VARCHAR(80) NOT NULL,
    version_no INT NOT NULL,
    parser_mode VARCHAR(80) NOT NULL,
    provider_mode VARCHAR(80) NOT NULL,
    prompt_version VARCHAR(80) NOT NULL,
    schema_version VARCHAR(80) NOT NULL,
    extracted_requirements_json TEXT NOT NULL,
    keywords_json TEXT NOT NULL,
    risk_terms_json TEXT NOT NULL,
    sanitized_text TEXT NOT NULL,
    parse_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS jd_evidence_binding (
    id VARCHAR(100) PRIMARY KEY,
    job_id VARCHAR(80) NOT NULL,
    parse_version_id VARCHAR(80) NOT NULL,
    requirement_key VARCHAR(80) NOT NULL,
    requirement_label VARCHAR(160) NOT NULL,
    evidence_id VARCHAR(80) NOT NULL,
    evidence_strength VARCHAR(40) NOT NULL,
    binding_reason TEXT NOT NULL,
    evidence_source VARCHAR(160) NOT NULL,
    review_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS jd_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    job_id VARCHAR(80) NOT NULL,
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(40) NOT NULL,
    next_status VARCHAR(40) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    changed_fields_json TEXT NOT NULL,
    before_snapshot_json TEXT NOT NULL,
    after_snapshot_json TEXT NOT NULL,
    human_note TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS match_report (
    id VARCHAR(80) PRIMARY KEY,
    job_id VARCHAR(80) NOT NULL,
    mode VARCHAR(40) NOT NULL,
    score INT NOT NULL,
    skill_score INT NOT NULL,
    evidence_score INT NOT NULL,
    risk_score INT NOT NULL,
    interview_score INT NOT NULL,
    recommended_resume VARCHAR(160) NOT NULL,
    status VARCHAR(80) NOT NULL,
    summary_json TEXT NOT NULL,
    score_json TEXT NOT NULL,
    evidence_sources_json TEXT NOT NULL,
    skill_gaps_json TEXT NOT NULL,
    recommended_actions_json TEXT NOT NULL,
    trace_evidence_json TEXT NOT NULL,
    disclaimer TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS match_report_version (
    id VARCHAR(80) PRIMARY KEY,
    report_id VARCHAR(80) NOT NULL,
    job_id VARCHAR(80) NOT NULL,
    parse_version_id VARCHAR(80) NOT NULL,
    version_no INT NOT NULL,
    score INT NOT NULL,
    skill_score INT NOT NULL,
    evidence_score INT NOT NULL,
    risk_score INT NOT NULL,
    interview_score INT NOT NULL,
    recommended_resume VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    summary_json TEXT NOT NULL,
    score_breakdown_json TEXT NOT NULL,
    evidence_refs_json TEXT NOT NULL,
    skill_gaps_json TEXT NOT NULL,
    recommended_actions_json TEXT NOT NULL,
    risk_notes_json TEXT NOT NULL,
    generated_by VARCHAR(120) NOT NULL,
    provider_mode VARCHAR(80) NOT NULL,
    prompt_version VARCHAR(80) NOT NULL,
    schema_version VARCHAR(80) NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    human_review_id VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS match_report_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    report_version_id VARCHAR(80) NOT NULL,
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(40) NOT NULL,
    next_status VARCHAR(40) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    changed_fields_json TEXT NOT NULL,
    human_note TEXT NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    copy_allowed BOOLEAN,
    copy_reason TEXT,
    version_status VARCHAR(40),
    human_review_status VARCHAR(40),
    boundary_notice TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS copy_permission_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    action VARCHAR(40) NOT NULL,
    allowed BOOLEAN NOT NULL,
    reason TEXT NOT NULL,
    target_status VARCHAR(40) NOT NULL,
    human_review_status VARCHAR(40) NOT NULL,
    schema_validated BOOLEAN NOT NULL,
    risk_guard_passed BOOLEAN NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    provider_run_id VARCHAR(100) NOT NULL,
    boundary_notice TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS permission_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    allowed BOOLEAN NOT NULL,
    reason TEXT NOT NULL,
    boundary_notice TEXT NOT NULL,
    request_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS interview_prep (
    id VARCHAR(80) PRIMARY KEY,
    job_id VARCHAR(80) NOT NULL,
    mode VARCHAR(40) NOT NULL,
    positioning_notice TEXT NOT NULL,
    focus_areas_json TEXT NOT NULL,
    questions_json TEXT NOT NULL,
    star_draft_json TEXT NOT NULL,
    risk_notes_json TEXT NOT NULL,
    review_timeline_json TEXT NOT NULL,
    review_status VARCHAR(80) NOT NULL,
    disclaimer TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS application_record (
    id VARCHAR(80) PRIMARY KEY,
    company VARCHAR(160) NOT NULL,
    role_title VARCHAR(160) NOT NULL,
    city VARCHAR(80) NOT NULL,
    resume_version VARCHAR(120) NOT NULL,
    source_note TEXT NOT NULL,
    status VARCHAR(80) NOT NULL,
    next_action TEXT NOT NULL,
    timeline_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS human_review_item (
    id VARCHAR(80) PRIMARY KEY,
    review_type VARCHAR(80) NOT NULL,
    title VARCHAR(180) NOT NULL,
    risk_level VARCHAR(40) NOT NULL,
    source_page VARCHAR(80) NOT NULL,
    provider_mode VARCHAR(80) NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    original_text TEXT NOT NULL,
    jd_snippet TEXT NOT NULL,
    risk_terms_json TEXT NOT NULL,
    evidence_refs_json TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    reviewer VARCHAR(120) NOT NULL,
    human_note TEXT NOT NULL,
    evidence_note TEXT NOT NULL,
    last_action TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS human_review_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    review_id VARCHAR(80) NOT NULL,
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(40) NOT NULL,
    next_status VARCHAR(40) NOT NULL,
    previous_risk_level VARCHAR(40) NOT NULL,
    next_risk_level VARCHAR(40) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    human_note TEXT NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    trace_hash VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS provider_trace_run (
    id VARCHAR(80) PRIMARY KEY,
    run_id VARCHAR(100) NOT NULL UNIQUE,
    job_title VARCHAR(160) NOT NULL,
    provider_mode VARCHAR(80) NOT NULL,
    final_provider VARCHAR(120) NOT NULL,
    model VARCHAR(120) NOT NULL,
    fallback_reason TEXT NOT NULL,
    prompt_version VARCHAR(80) NOT NULL,
    schema_version VARCHAR(80) NOT NULL,
    risk_flags_json TEXT NOT NULL,
    evidence_count INT NOT NULL,
    human_review_status VARCHAR(80) NOT NULL,
    duration_ms INT NOT NULL,
    trace_hash VARCHAR(120) NOT NULL,
    evidence_detail_json TEXT NOT NULL,
    technical_tags_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS trace_step (
    id VARCHAR(80) PRIMARY KEY,
    run_id VARCHAR(100) NOT NULL,
    step_order INT NOT NULL,
    step_key VARCHAR(80) NOT NULL,
    step_name VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    duration_ms INT NOT NULL,
    input_summary TEXT NOT NULL,
    output_summary TEXT NOT NULL,
    evidence_refs_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
