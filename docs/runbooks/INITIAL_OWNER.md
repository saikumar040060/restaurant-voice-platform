# Initial owner provisioning

Implemented as an operator-only `bootstrap` profile. There is deliberately no public signup endpoint and no default password. Use only an approved empty installation. For this task it was exercised only with fictional disposable test data; no real owner account has been provisioned.

Build the JAR with `python3 scripts/test_backend.py` after selecting Java 21. Configure `VOICE_DB_URL`, `VOICE_DB_USER`, and `VOICE_DB_PASSWORD` through the environment secret mechanism. Use a dedicated migration/provisioning identity; the production runtime must not own the schema.

Create a UTF-8 file containing the chosen password through the environment's secure secret-entry mechanism, with permissions 0600. Never paste a password into chat, command-line arguments, source, or logs. The password must be at least 12 characters and at most 72 UTF-8 bytes; an optional final newline is ignored. Set `VOICE_BOOTSTRAP_PASSWORD_FILE` to that file path. Set `VOICE_BOOTSTRAP_TENANT` and `VOICE_BOOTSTRAP_USERNAME` to the approved name and owner username.

Run:

```sh
java -jar apps/backend/target/restaurant-voice-platform-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=bootstrap \
  --spring.main.web-application-type=none
```

This command runs migrations, locks provisioning, verifies that no tenant exists, creates one tenant and BCrypt-hashed owner, records an audit event, closes the application, and exits. Repeat execution fails rather than creating another owner. It rejects a password file readable by group/others. POSIX file permissions are currently required; Windows support is not implemented.

Remove the transient secret file through the approved secret-management mechanism after successful setup. Start the normal application without the bootstrap profile. Owner login returns a short-lived bearer session. Management APIs are owner-only; staff roles are limited to MANAGER/EMPLOYEE. A secure invitation/password-reset workflow is future work; do not transmit employee passwords through chat or ordinary email.

This is initial installation provisioning, not general multi-tenant self-service onboarding. Additional tenant onboarding, ownership transfer, owner recovery, MFA enrollment, and browser onboarding UI remain unimplemented. Never point this command at production without approval.
