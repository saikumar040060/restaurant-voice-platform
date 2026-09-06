# Contributing

Use milestone branches and focused commits. Explain the user-visible problem, resulting behavior, validation evidence, and residual risks in reviews. Follow AGENTS.md. A milestone is incomplete if any required check fails.

Java uses four-space indentation and explicit imports; TypeScript uses two spaces. UTF-8 files end in one newline, without trailing spaces. Avoid wildcard public APIs. Pin dependencies; commit frontend lockfiles when the frontend is introduced. Database changes use forward Flyway migrations; document rollback through restore or compensating migration and review destructive changes explicitly.

Test money, state transitions, authorization, failure recovery, and external effects as behavior. Do not substitute mocks for PostgreSQL isolation or provider contract evidence. Never include customer data in fixtures.
