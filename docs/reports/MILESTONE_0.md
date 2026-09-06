# Milestone 0 evidence report

Date: 2026-09-05. Status: NOT COMPLETE — remote CI has not run; repository selection is pending. Local foundation checks pass.

## Outcome and components

Created a new isolated Git repository on milestone/0-foundation. Preserved the original handoff verbatim as PRODUCT_SPEC.md and recorded the conversational knowledge expansion in docs/PRODUCT_ADDENDUM.md. Added engineering rules, architecture/ADR template, data-flow/trust boundaries, preliminary threat model/access matrix, operational runbook skeletons, test strategy, milestone status, integrations register, CI skeleton, PostgreSQL Compose configuration, empty OpenAPI contract, and Java 21 Spring Boot bootstrap. Dashboard and load/end-to-end directories explicitly document unimplemented scope.

## Decisions and improvements

Stack: Java 21, Spring Boot 3.5.14, Maven 3.9.12, PostgreSQL 17.6; planned Spring Security/JPA/Flyway and React/TypeScript. Backend has no HTTP listeners or provider calls. Java 21 is selected per process using Homebrew, leaving global Java settings untouched.

Added approved/provenanced restaurant knowledge and dish Q&A evaluation to the milestone plan. Clarified that an outbox alone cannot guarantee exactly-once provider effects. Highlighted unsolicited sensitive speech and provider retention controls in the threat model. No scope-changing vendors or costs were introduced.

## Commands and results

Working directory is repository root unless indicated.

- `python3 scripts/validate_foundation.py`: exit 0. Foundation required-file, text formatting, POM/Java-target, and private-key-marker checks pass. Exact file count changes when this report is added.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -o -B -f apps/backend/pom.xml verify`: initial exit 1, missing cached Surefire dependencies. Compilation passed; tests did not execute in that attempt.
- After owner-granted network/cache access, `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -B -f apps/backend/pom.xml verify`: exit 0, BUILD SUCCESS, one startup test, zero failures/errors/skips. This is only a foundation smoke test.
- `VOICE_DB_PASSWORD=local-config-validation-only docker compose -f infra/local/compose.yaml config --quiet`: exit 0. Configuration validation only; no database container started.
- `git diff --cached --check`: run immediately before commit; result recorded in task output.

## Acceptance evidence and limits

Compilation, startup test, foundation formatting/integrity, and Compose validation pass locally. The CI definition exists, but no hosted run exists. Therefore the milestone is not complete under the original handoff's CI criterion. The local Docker daemon was inaccessible from the sandbox; database startup/restore was not validated. No migration, endpoint, authentication, tenant isolation, conversation, voice, POS, or UI functionality is implemented. Full secret/SAST/dependency/container scans, SBOM, image digests, and independent security review remain future gates. The marker check is not a comprehensive secret scanner.

## Next milestone and approvals

Next: owner-selected GitHub repository, branch push and successful CI; then Milestone 1 secure multi-tenant foundation. No product credentials, paid services, or real customer activity are needed now. Do not paste credentials into chat. Repository choice is the pending external dependency, not an authorization request to build ordinary local code.

## Commit and continuation

The foundation commit is the commit containing this report; resolve with `git log -1 --format=%H -- docs/reports/MILESTONE_0.md`. The task's final response records its SHA. No PR link exists because there is no remote.

Local execution only. The laptop must remain awake during local work. All required source is in this repository; push it and configure a reproducible cloud task before laptop-independent execution. No cloud execution or phone notification capability is claimed configured.

## Integrations

No plugin installed or product account connected. Existing GitHub connector is available; the minimum next scope is the selected repository's code/PR/CI access. Square, voice, telephony, SMS, and hosting remain unconnected. See docs/INTEGRATIONS.md.
