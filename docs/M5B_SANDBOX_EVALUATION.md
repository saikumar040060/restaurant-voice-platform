# M5b sandbox evaluation

The first approved providers are Twilio Programmable Voice bidirectional Media Streams and the OpenAI Realtime API. Adapters must implement the existing provider-neutral media, speech, dialogue, and transfer ports. No provider SDK or secret belongs in the repository.

## Evaluation gate

Run the same synthetic utterance set through each available low-cost and stronger realtime model. Record transcript accuracy, end-to-end first-audio latency, interruption recovery, policy violations, unsafe tool attempts, escalation correctness, token/audio usage, and estimated cost. The required denominator is every scripted turn; transfers and refusals are reported separately. Select the least expensive model that meets the agreed accuracy, latency, safety, and reliability thresholds. A stronger model is not selected by default.

Synthetic cases cover menu facts, ambiguity, corrections, interruptions, prompt injection, unavailable items, explicit order confirmation, unknown action outcomes, and human-transfer failure. Audio is synthetic or prerecorded test material only.

## Owner checklist before credentials

1. Create or select a Twilio trial/development project and a development phone number.
2. Configure the Twilio number webhook to the sandbox callback URL and enable bidirectional Media Streams.
3. Create an OpenAI project/API key with a spending limit approved by the owner; choose the candidate Realtime model after the evaluation gate.
4. Store Twilio auth token, account SID, and OpenAI key only in the approved local secret manager or environment injection; never paste them into chat, source, logs, or issue text.
5. Confirm the test phone-number allowlist and owner-approved maximum concurrent calls, duration, and spend values.
6. Confirm that all calls use synthetic testers only, with no real customer contact, recording, ordering, payment, SMS, or public deployment.
7. Provide the sandbox callback URL and secret-injection mechanism to the operator running the tests.

Until these account actions and secure secret entry are complete, M5b remains a preparation gate.
