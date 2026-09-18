# Condense

Plain-English digests of SEC filings, delivered by email. A subscriber gives an address
and a list of tickers; Condense polls EDGAR for their filings, summarizes each one with a
self-hosted model, and sends a digest. There is no dashboard to log into.

Condense reports what filings say. It does not give investment advice, make predictions,
or characterize filings as good or bad news. That constraint is enforced in the prompt and
is the product's whole differentiator — keep it.

## Stack

| Concern | Choice |
| --- | --- |
| Runtime | Java 21, Spring Boot 4.1 |
| Build | Maven (wrapper committed) |
| Database | PostgreSQL, schema owned by Flyway, JPA set to `validate` |
| Templating | Thymeleaf, used for email bodies only |
| Inference | Ollama behind the `Summarizer` port |
| Email | `EmailSender` port; no provider wired yet |
| Sessions | Spring Session JDBC, schema owned by Flyway |
| Hosting | Render (web service + managed Postgres), Ollama self-hosted |

## Layout

```
io.condense
  auth/        magic links, session minting, route guard
  company/     ticker to CIK directory
  config/      typed configuration properties and HTTP clients
  digest/      digest assembly, delivery, and per-subscriber delivery log
  email/       EmailSender port, renderer, logging implementation
  filing/      Filing and FilingSummary entities
  ingest/      EDGAR client, ingestion job, market signal ports
  subscriber/  subscribers, subscriptions, plan limits, public API
  summarize/   Summarizer port, Ollama implementation, chunking, prompts
  support/     shared utilities and API error handling
```

Ports worth knowing about, because everything swappable hangs off them:

- `Summarizer` — `OllamaSummarizer` by default, `EchoSummarizer` when `condense.summarizer=echo`.
- `EmailSender` — `LoggingEmailSender` by default. A real provider is a new implementation
  plus `condense.email.provider`; nothing else changes.
- `SignalProvider` — news, price moves, and earnings calls land in `market_signal`. No
  implementations yet; the table and port exist so adding one is additive.

## Access without registration

`POST /api/subscriptions` takes an email and tickers and sends a confirmation link.
`GET /auth/verify?token=` consumes a single-use 24-hour token, mints a session cookie, and
redirects to `/dashboard`. `POST /auth/request-link` mails a fresh link and always answers 202,
so it cannot be used to discover which addresses are subscribed. `GET /api/me` is the
session-guarded endpoint behind the dashboard. Unsubscribe stays session-free and long-lived so
one-click unsubscribe keeps working from any old email.

The service is free and ad-funded — there is no billing code and no paid tier.

## Running locally

Everything in containers:

```bash
export EDGAR_USER_AGENT="Your Name (you@example.com)"
docker compose up --build
```

That starts Postgres, Ollama, and the app on http://localhost:8080. It defaults to
`CONDENSE_SUMMARIZER=echo` so nothing needs a model pulled. For real summaries, pull a model
into the Ollama container and set `CONDENSE_SUMMARIZER=ollama`.

Pages you can click through:

| Path | What it is |
| --- | --- |
| `/` | Signup: email, ticker search across ~10,400 tickers, delivery cadence |
| `/dashboard` | Session-guarded settings: add and remove tickers, change cadence, sign out |
| `/dev/inbox` | Captured emails, with sign-in links made clickable |

`/dev/inbox` exists only while `condense.email.provider` is `dev-inbox`, which the `local`
profile sets. Nothing is actually sent yet, so this is how you complete the sign-in loop in a
browser. It disappears in any other profile.

These pages are a working harness, not the product's design — no visual identity, no logo, no
imagery.

Or run the app from your IDE against containerized dependencies:

```bash
docker compose up -d postgres ollama
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On first start the app downloads the SEC ticker directory (~10,400 tickers across ~8,000
companies) and fills `company` and `company_ticker`. It only does this when `company` is empty,
so restarts are free. A company can list under several tickers — GOOG and GOOGL both resolve to
Alphabet — so subscriptions are stored against the CIK and displayed under the primary listing.

The `local` profile points at the compose Postgres, uses `EchoSummarizer` so you do not
need a model pulled, and logs emails instead of sending them. To exercise the real path,
`docker compose exec ollama ollama pull llama3.1:8b` and set `condense.summarizer=ollama`.

Set a real `EDGAR_USER_AGENT` before hitting EDGAR. The SEC requires a descriptive
User-Agent with a contact address and will block you without one. `EdgarClient` also
rate-limits itself to stay under the SEC's ten-requests-per-second ceiling.

## Tests

```bash
./mvnw test
```

`CondenseApplicationTests` uses Testcontainers and needs a Docker daemon. It is the test
that catches entity/migration drift, because JPA runs with `ddl-auto: validate` — if an
entity and its migration disagree, the context fails to start.

## Deploying

`render.yaml` provisions the web service and the database. The app is deployed from the
Dockerfile.

Two things to know about this topology:

1. Render's free tier spins services down when idle, and a spun-down service runs no
   scheduled jobs. The digest schedulers need an always-on instance.
2. Ollama runs on your own hardware, not on Render. `OLLAMA_BASE_URL` must point at a
   tunnel (Cloudflare Tunnel or Tailscale) back to that machine, and that tunnel is a
   single point of failure for summarization. Ingestion is decoupled from summarization
   in the schema — filings are stored before they are summarized — so an outage delays
   digests rather than losing filings.

## Conventions

- No comments in source. Names and small methods carry the meaning.
- Entities have a protected no-arg constructor and no setters; state changes go through
  named methods (`confirm()`, `unsubscribe()`, `upgradeTo(...)`).
- Schema changes are new Flyway migrations. Never edit an applied migration.
- Configuration is bound to records in `config/`, never read via raw `@Value`.
- Tests use JUnit 5 and AssertJ with hand-written fakes. No mocking framework.
