# Data flow and trust boundaries

```mermaid
flowchart LR
  Caller[Caller / untrusted speech] --> Tel[Telephony adapter]
  Tel --> Verify[Webhook verification and replay guard]
  Verify --> Session[Tenant-scoped call session]
  Session --> Voice[STT / dialogue / TTS]
  Knowledge[Approved tenant knowledge and menu] --> Voice
  Voice --> Proposal[Structured proposal]
  Proposal --> Gate[Schema / authorization / freshness / policy / confirmation]
  Gate --> DB[(Tenant-scoped PostgreSQL)]
  DB --> Outbox[Durable outbox]
  Outbox --> Adapter[Narrow provider adapters]
  Adapter --> POS[POS / hosted checkout / SMS]
  Staff[Authenticated staff] --> Auth[RBAC and tenant authorization]
  Auth --> DB
  Gate --> Audit[Redacted append-only audit]
```

Boundaries: public caller/provider input to verified adapter; model and retrieval output to trusted policy code; authenticated dashboard to tenant services; database transaction to external providers; application to third-party voice processing. Validate at each boundary. Telephony routing determines restaurant context from trusted configuration, never caller-provided tenant IDs. Credentials remain server-side.

Raw audio and transcripts may contain unsolicited sensitive data. Minimize capture, avoid recording by default, redact before persistence, and configure provider retention. A prompt alone cannot prevent a caller speaking card details. Provider data handling and legal review remain mandatory before live calls.
