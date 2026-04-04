-- Second Bloom MVP backend Postgres schema draft
-- Status: design draft only, not applied by this Android repository.

create extension if not exists pgcrypto;

create table if not exists analysis_requests (
    id uuid primary key default gen_random_uuid(),
    analysis_id text not null unique,
    request_received_at timestamptz not null default now(),
    source_filename text not null,
    source_mime_type text not null,
    source_size_bytes bigint,
    image_object_key text not null,
    background_complexity text,
    provider_name text not null,
    provider_model text not null,
    status text not null,
    error_code text,
    error_message text,
    latency_ms integer,
    constraint analysis_requests_status_check
        check (status in ('pending', 'succeeded', 'failed')),
    constraint analysis_requests_background_complexity_check
        check (
            background_complexity is null
            or background_complexity in ('low', 'high')
        )
);

create table if not exists analysis_results (
    analysis_id text primary key references analysis_requests (analysis_id) on delete cascade,
    garment_type text not null,
    color text not null,
    material text not null,
    style text not null,
    confidence numeric(4,3),
    warnings_json jsonb not null default '[]'::jsonb,
    defects_json jsonb not null default '[]'::jsonb,
    raw_provider_payload_json jsonb,
    created_at timestamptz not null default now(),
    constraint analysis_results_confidence_check
        check (confidence is null or (confidence >= 0 and confidence <= 1))
);

create table if not exists plan_requests (
    id uuid primary key default gen_random_uuid(),
    analysis_id text references analysis_requests (analysis_id) on delete set null,
    intent text not null,
    user_preferences text,
    provider_name text not null,
    provider_model text not null,
    status text not null,
    error_code text,
    error_message text,
    latency_ms integer,
    request_received_at timestamptz not null default now(),
    constraint plan_requests_status_check
        check (status in ('pending', 'succeeded', 'failed')),
    constraint plan_requests_intent_check
        check (intent in ('daily', 'occasion', 'diy', 'size_adjustment'))
);

create table if not exists plan_results (
    id uuid primary key default gen_random_uuid(),
    plan_id text not null unique,
    plan_request_id uuid not null references plan_requests (id) on delete cascade,
    ordinal integer not null,
    title text not null,
    summary text not null,
    difficulty text not null,
    materials_json jsonb not null default '[]'::jsonb,
    estimated_time text not null,
    steps_json jsonb not null default '[]'::jsonb,
    reasoning_note text,
    raw_provider_payload_json jsonb,
    created_at timestamptz not null default now(),
    constraint plan_results_difficulty_check
        check (difficulty in ('easy', 'medium', 'hard')),
    constraint plan_results_ordinal_check
        check (ordinal >= 0)
);

create table if not exists preview_jobs (
    id uuid primary key default gen_random_uuid(),
    preview_job_id text not null unique,
    render_mode text not null,
    status text not null,
    requested_plan_ids_json jsonb not null default '[]'::jsonb,
    requested_plan_count integer not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    expires_at timestamptz not null,
    error_code text,
    error_message text,
    constraint preview_jobs_render_mode_check
        check (render_mode = 'simulation'),
    constraint preview_jobs_status_check
        check (
            status in (
                'queued',
                'running',
                'completed',
                'completed_with_failures',
                'failed',
                'expired'
            )
        ),
    constraint preview_jobs_requested_plan_count_check
        check (requested_plan_count between 1 and 3)
);

create table if not exists preview_job_renders (
    id uuid primary key default gen_random_uuid(),
    preview_job_id text not null references preview_jobs (preview_job_id) on delete cascade,
    plan_id text not null references plan_results (plan_id) on delete cascade,
    render_status text not null,
    filtered_reason text,
    failure_message text,
    before_image_object_key text,
    after_image_object_key text,
    comparison_image_object_key text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint preview_job_renders_status_check
        check (render_status in ('queued', 'running', 'completed', 'failed', 'filtered'))
);

create index if not exists idx_analysis_requests_received_at
    on analysis_requests (request_received_at desc);

create index if not exists idx_analysis_requests_status
    on analysis_requests (status);

create index if not exists idx_plan_requests_received_at
    on plan_requests (request_received_at desc);

create index if not exists idx_plan_requests_status
    on plan_requests (status);

create index if not exists idx_plan_requests_analysis_id
    on plan_requests (analysis_id);

create unique index if not exists uq_plan_results_request_ordinal
    on plan_results (plan_request_id, ordinal);

create index if not exists idx_preview_jobs_status
    on preview_jobs (status);

create index if not exists idx_preview_jobs_expires_at
    on preview_jobs (expires_at);

create index if not exists idx_preview_job_renders_preview_job_id
    on preview_job_renders (preview_job_id);

create index if not exists idx_preview_job_renders_plan_id
    on preview_job_renders (plan_id);

create unique index if not exists uq_preview_job_renders_job_plan
    on preview_job_renders (preview_job_id, plan_id);
