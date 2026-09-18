create extension if not exists "pgcrypto";

create table company (
    cik            varchar(10) primary key,
    ticker         varchar(16) not null unique,
    name           text        not null,
    updated_at     timestamptz not null default now()
);

create table subscriber (
    id                  uuid primary key default gen_random_uuid(),
    email               text        not null unique,
    status              varchar(32) not null,
    cadence             varchar(32) not null,
    unsubscribe_token   varchar(64) not null unique,
    created_at          timestamptz not null default now(),
    confirmed_at        timestamptz
);

create table subscription (
    id             uuid primary key default gen_random_uuid(),
    subscriber_id  uuid        not null references subscriber (id) on delete cascade,
    cik            varchar(10) not null references company (cik),
    created_at     timestamptz not null default now(),
    unique (subscriber_id, cik)
);

create index subscription_cik_idx on subscription (cik);

create table magic_link_token (
    id             uuid primary key default gen_random_uuid(),
    subscriber_id  uuid        not null references subscriber (id) on delete cascade,
    token_hash     varchar(64) not null unique,
    purpose        varchar(32) not null,
    expires_at     timestamptz not null,
    consumed_at    timestamptz,
    created_at     timestamptz not null default now()
);

create table filing (
    id                   uuid primary key default gen_random_uuid(),
    cik                  varchar(10) not null references company (cik),
    accession_number     varchar(32) not null unique,
    form_type            varchar(16) not null,
    filed_on             date        not null,
    period_end           date,
    primary_document_url text        not null,
    description          text,
    discovered_at        timestamptz not null default now()
);

create index filing_cik_filed_on_idx on filing (cik, filed_on desc);

create table filing_summary (
    id             uuid primary key default gen_random_uuid(),
    filing_id      uuid        not null unique references filing (id) on delete cascade,
    model          varchar(64) not null,
    headline       text        not null,
    bullets        text        not null,
    significance   varchar(16) not null,
    source_chars   integer     not null,
    created_at     timestamptz not null default now()
);

create table market_signal (
    id             uuid primary key default gen_random_uuid(),
    cik            varchar(10) not null references company (cik),
    source         varchar(32) not null,
    kind           varchar(32) not null,
    headline       text        not null,
    url            text,
    occurred_at    timestamptz not null,
    created_at     timestamptz not null default now()
);

create index market_signal_cik_occurred_idx on market_signal (cik, occurred_at desc);

create table delivery_log (
    id                  uuid primary key default gen_random_uuid(),
    subscriber_id       uuid        not null references subscriber (id) on delete cascade,
    digest_date         date        not null,
    status              varchar(32) not null,
    provider_message_id text,
    failure_reason      text,
    created_at          timestamptz not null default now(),
    unique (subscriber_id, digest_date)
);
