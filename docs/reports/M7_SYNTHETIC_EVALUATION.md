# M7 synthetic evaluation evidence

Date: 2026-09-08

This report covers local fixtures only. It contains no provider audio, caller data, restaurant data, calls, recordings, orders, payments, or measured realtime-model cost.

| Check | Evidence | Result |
| --- | --- | --- |
| Restaurant fixture menu/modifier pricing | `HarborPizzaFixtureTest`, `OrderPricingTest` | Deterministic fictional prices and modifier validation pass. |
| Read-back/confirmation | `RestaurantReadbackTest`, `RestaurantOrderWorkflowTest` | Submission requires an uninterrupted delivered read-back and explicit confirmation. |
| Voice failure behavior | `VoiceTurnServiceTest` | Synthetic dialogue failure creates a scoped mock transfer case and emits no invented audio. |
| Five restaurant calls at one location | `FixtureCallHarnessTest` | Five concurrent synthetic restaurant calls remain isolated. |
| Ten mixed calls | `FixtureCallHarnessTest` | Ten concurrent reference/restaurant synthetic calls remain isolated. |

The scripted fixture tests do **not** establish the M7 98% real-voice line-item/modifier accuracy target. That target needs an owner-approved provider evaluation with measured audio input/output, latency, transfer rate, and cost. Twilio is disabled and no real call was made.
