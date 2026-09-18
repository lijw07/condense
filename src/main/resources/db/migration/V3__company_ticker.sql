create table company_ticker (
    ticker     varchar(16) primary key,
    cik        varchar(10) not null references company (cik) on delete cascade,
    created_at timestamptz not null default now()
);

create index company_ticker_cik_idx on company_ticker (cik);

insert into company_ticker (ticker, cik)
select ticker, cik from company
on conflict (ticker) do nothing;
