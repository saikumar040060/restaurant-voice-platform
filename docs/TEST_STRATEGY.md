# Test strategy

Sequencing update, 2026-09-06: [MILESTONES.md](MILESTONES.md) is authoritative for M2–M7 allocation and scenario coverage. The paragraphs below retain the original sequence and test categories. Revised M2 tests generic tenancy/principals and module boundaries; M3 call/action durability; M4 knowledge/dialogue; M5 media and measured voice; M6 restaurant rules; M7 integrated operations. M8–M11 retain sandbox, recovery, hardening and pilot gates.

Milestone 0: offline foundation integrity and formatting, Maven compilation/context startup, Compose configuration validation, CI workflow definition. A remote CI run requires a selected pushed GitHub repository.

Milestone 1: PostgreSQL-backed positive/negative role tests, tenant isolation through repository/service/API and jobs, session security, audited outcomes. H2 is not a substitute for PostgreSQL-specific guarantees.

Milestones 2–5: exact money/rounding, knowledge provenance/freshness, legal state transitions, confirmation snapshots, idempotency under concurrency, webhook replay, crash recovery, ambiguous outcomes, and all applicable handoff scenarios.

Milestones 6–8: browser accessibility and role/navigation tests, reconnect authorization, synthetic audio and interruptions, grounded knowledge evaluation, official sandbox contracts when approved.

Milestones 9–11: load/soak at declared capacity, backup restoration, security scanners/SBOM, adverse provider failures, controlled pilot evaluation. Include all 30 mandatory handoff scenarios. Require zero duplicates, cross-tenant access, and prohibited actions in the approved suite; at least 98% line-item/modifier accuracy. Define dataset, denominator, transfer rate, and uncertainty cases before claiming accuracy. No availability claim without monitoring evidence.

Record commands, exit codes, counts, environment, commit, limitations, and saved reports. No passing skeleton test can establish product readiness.
