# M5b sandbox evaluation

The first approved providers are Twilio Programmable Voice bidirectional Media Streams and the OpenAI Realtime API. Adapters must implement the existing provider-neutral media, speech, dialogue, and transfer ports. No provider SDK or secret belongs in the repository.

## Evaluation gate

Run the same synthetic utterance set through each available low-cost and stronger realtime model. Record transcript accuracy, end-to-end first-audio latency, interruption recovery, policy violations, unsafe tool attempts, escalation correctness, token/audio usage, and estimated cost. The required denominator is every scripted turn; transfers and refusals are reported separately. Select the least expensive model that meets the agreed accuracy, latency, safety, and reliability thresholds. A stronger model is not selected by default.

Synthetic cases cover menu facts, ambiguity, corrections, interruptions, prompt injection, unavailable items, explicit order confirmation, unknown action outcomes, and human-transfer failure. Audio is synthetic or prerecorded test material only.

`RestaurantEvaluationCatalog` contains the 30 mandatory fictional restaurant scenarios as the fixed evaluation denominator. Each result must report its scenario ID and expected outcome; real voice measurements must not replace this catalog with a smaller hand-picked set.

## Account and secret state

The owner has created sandbox Twilio and OpenAI accounts and injected the required credentials and a single test-caller allowlist as masked Railway variables. Values were not read, copied, logged, or committed. The Railway health endpoint supplies the future HTTPS callback base.

Before a provider connection is enabled, finish the local adapter-contract work, choose explicit safety limits, and obtain a specific owner decision to connect the sandbox providers. Keep webhook and realtime feature flags disabled until then. No real customers, restaurant orders, payments, SMS, recordings, or public service use is authorized.

## Selection implementation

The local `RealtimeModelSelector` takes measured candidate aggregates and selects the lowest estimated-cost candidate that meets every owner-approved threshold. Any safety violation disqualifies a candidate. It does not name a model, make an API request, inspect secrets, or enable a provider. Its test fixture uses a 98% accuracy floor, 1.5-second P95 first-audio limit, one allowed interruption failure, and zero reliability failures; the owner must approve final thresholds before a real evaluation.

## Initial sandbox candidate

With the owner's 2026-09-08 connection authorization, `gpt-realtime-2.1-mini` is configured as the initial low-cost candidate for a controlled synthetic evaluation. OpenAI documents it as a lower-cost Realtime model with WebRTC, WebSocket, and SIP support; its listed audio rates are lower than `gpt-realtime-2.1`. This is an evaluation candidate only, not a production selection. The measured evaluation gate remains responsible for accepting or rejecting it, and Twilio ingress remains disabled.
