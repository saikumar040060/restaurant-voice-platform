# Twilio sandbox activation checklist

This runbook is for the owner's personal, allowlisted sandbox phone only. It does not authorize restaurant numbers, customers, recordings, outbound messages, payments, orders, public launch, or a production deployment.

## Preconditions

- The current backend branch has passed `python3 scripts/test_backend.py` using disposable PostgreSQL.
- Railway deployment is healthy at `/healthz` and `VOICE_TWILIO_ENABLED` is still `false`.
- `VOICE_OPENAI_REALTIME_ENABLED` and `VOICE_OPENAI_CONNECTIVITY_PROBE_ENABLED` are `false` unless separately approved for a bounded test.
- The owner has selected one personal E.164 test number and an approved spend ceiling.
- Twilio trial/account geographic permissions and verified caller requirements have been checked by the owner.

## Secure Railway variables

Enter these only in Railway's masked Variables form. Never paste them into chat, source control, logs, or a command line.

| Variable | Required value |
| --- | --- |
| `VOICE_TWILIO_AUTH_TOKEN` | Current Twilio Auth Token |
| `VOICE_TWILIO_PUBLIC_BASE_URL` | `https://restaurant-voice-platform-production.up.railway.app` |
| `VOICE_TWILIO_ALLOWED_FROM` | The one approved personal E.164 test number |
| `VOICE_TWILIO_REPLAY_WINDOW_SECONDS` | `300` |
| `VOICE_TWILIO_ENABLED` | Keep `false` until the owner explicitly approves the one-call test |

`TWILIO_ACCOUNT_SID` may remain stored for later adapter work, but the current disabled ingress does not use it to place calls.

## Twilio Console configuration

After an explicit one-call-test approval, configure the sandbox phone number's **Voice** webhook to:

- Method: `POST`
- URL: `https://restaurant-voice-platform-production.up.railway.app/webhooks/twilio/voice`

Do not configure messaging, status callbacks, recording, SIP, forwarding, payment, or any public number routing as part of this step.

## Controlled test and rollback

1. Set `VOICE_TWILIO_ENABLED=true` in Railway and deploy once.
2. Make exactly one call from the allowlisted personal number.
3. Expect the current safe ingress to validate signature/replay/allowlist and return a temporary-unavailable response. It must not start a media stream, record audio, create an order, send a message, or contact an employee.
4. Check Railway logs for only redacted operational status; never reveal secrets or caller data in a report.
5. Immediately set `VOICE_TWILIO_ENABLED=false` and deploy again.
6. Confirm the endpoint returns disabled behavior and record the result in `PROJECT_STATUS.md`.

Stop if signature validation, caller allowlisting, replay protection, budget controls, deployment health, or rollback fails. Do not retry by broadening the allowlist or disabling a safety control.
