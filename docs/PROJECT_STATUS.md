# Project status

Updated: 2026-09-05.

## Current milestone

Milestone 1 — identity, owner bootstrap, staff/location administration, session revocation, and audit viewing are implemented and locally tested. Milestones 0 and 1 remain NOT COMPLETE; hosted CI is pending and Milestone 1 has remaining scope.

## Agreed scope

Natural spoken restaurant/dish Q&A, recommendations, follow-ups, and confirmed pickup orders. See PRODUCT_SPEC.md and PRODUCT_ADDENDUM.md.

## Milestones

| Milestone | Status |
|---|---|
| 0 Repository and engineering constitution | Local checks passed; remote CI pending |
| 1 Secure multi-tenant foundation | In progress: management and bootstrap slice tested |
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

Java 21 exists through Homebrew although the system Java 21 registration is broken and the default is Java 17. Select Java 21 per command without changing global settings. Maven 3.9.12 and Node 22.19.0 are installed. Docker access was granted for this turn. PostgreSQL 17.6 integration tests run in isolated disposable containers. Native PostgreSQL startup was blocked by sandbox shared-memory restrictions. Private repository created at https://github.com/saikumar040060/restaurant-voice-platform and configured as origin. It is empty: connector cannot access it, local Git authentication is unavailable, and no push/CI has occurred.

## Decisions and improvements

Knowledge provenance and dish/conversation evaluation are first-class requirements. Exactly-once effects depend on provider capabilities. Raw speech can contain unsolicited payment credentials, so provider retention/redaction must be evaluated as well as agent behavior.

## Next action

Resolve approved-repository publishing access, push source, and run hosted CI. Continue Milestone 1 with session rotation/retention, least-privilege database roles, security scans, and remaining isolation/permission checks. Owner onboarding and staff/location management are now implemented; see reports/MILESTONE_1_MANAGEMENT.md.

## Cloud/phone continuation

Local only. Laptop must stay awake. Nothing pushed; no cloud task configured. Repository is selected and created; publishing credentials/connector access must be resolved, then branch pushed and remote environment configured. No account credentials needed in chat.

## Implemented and verified

Spring Security opaque bearer sessions; BCrypt passwords; 15-minute expiry and immediate logout/disable/role enforcement; known-account login/logout audit; database-backed login throttling; restaurant/location tenant scope; owner-only restaurant creation with atomic audit; PostgreSQL composite tenant foreign keys; append-only UPDATE/DELETE audit trigger; generated correlation IDs; sanitized validation/database error responses. Twenty-eight PostgreSQL integration test cases pass, including 40 concurrent requests, audit-failure rollback, management permissions, and owner bootstrap. Packaged non-web bootstrap also passes success, repeat-rejection, and secret-file-permission checks. This is not a voice/order implementation.

Owner confirmed GitHub account saikumar040060 and explicitly approved creating private restaurant-voice-platform. Repository privacy verified in GitHub UI. A separate question requesting connector access to this repository is pending; no access expansion has been performed. Local code remains on milestone/1-tenant-foundation.
