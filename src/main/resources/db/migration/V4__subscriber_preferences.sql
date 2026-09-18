alter table subscriber
    add column summary_style      varchar(32) not null default 'KEY_POINTS_AND_NUMBERS',
    add column tone               varchar(32) not null default 'NEUTRAL_ANALYST',
    add column calendar_reminders boolean     not null default false,
    add column appearance         varchar(32) not null default 'DARK';
