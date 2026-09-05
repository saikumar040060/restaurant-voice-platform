# Project status

Updated: 2026-09-05.

## Current milestone

Milestone 0 — local foundation validated; NOT COMPLETE pending remote CI execution. No product features implemented.

## Agreed scope

Natural spoken restaurant/dish Q&A, recommendations, follow-ups, and confirmed pickup orders. See PRODUCT_SPEC.md and PRODUCT_ADDENDUM.md.

## Milestones

| Milestone | Status |
|---|---|
| 0 Repository and engineering constitution | Local checks passed; remote CI pending |
| 1 Secure multi-tenant foundation | Not started |
| 2 Menu, knowledge, business configuration | Not started |
| 3 Calls and deterministic simulator | Not started |
| 4 Orders and policy gateway | Not started |
| 5 Durable external effects | Not started |
| 6 Operations dashboard | Not started |
| 7 Provider-neutral voice | Not started |
| 8 Square sandbox | Not started |
| 9 Hardening and recovery | Not started |
| 10 Staging | Not started |
| 11 Pilot readiness | Not started |
| 12 Toast readiness and future verticals | Not started |

## Environment and dependencies

Java 21 exists through Homebrew although the system Java 21 registration is broken and the default is Java 17. Select Java 21 per command without changing global settings. Maven 3.9.12 and Node 22.19.0 are installed. Docker CLI exists; daemon access from sandbox is restricted. No GitHub remote is configured.

## Decisions and improvements

Knowledge provenance and dish/conversation evaluation are first-class requirements. Exactly-once effects depend on provider capabilities. Raw speech can contain unsolicited payment credentials, so provider retention/redaction must be evaluated as well as agent behavior.

## Next action

Owner to identify the GitHub repository (question sent). Then push the reviewed milestone branch, run CI, and record its result before Milestone 1. See reports/MILESTONE_0.md for exact local evidence. The original offline Maven failure was resolved with approved dependency download access.

## Cloud/phone continuation

Local only. Laptop must stay awake. Nothing pushed; no cloud task configured. Required before remote continuation: repository selection, branch push, reproducible environment. No account credentials needed in chat.
