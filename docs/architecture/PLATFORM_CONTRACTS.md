# Core and business-module contracts

Design contract v0.1, 2026-09-06. Pseudocode and requirements only; no Java interfaces or new HTTP endpoints are implemented by this document. See [ADR 0004](0004-platform-core-and-business-modules.md).

## Persisted concepts

| Contract | Required fields and invariants |
| --- | --- |
| Scope | tenant_id, business_id, location_id; every relationship validated server-side; no model-supplied scope is trusted |
| Principal | principal_id, tenant_id, kind, authorized capabilities, delegation reference; caller and staff identities are distinct |
| BusinessProfile | profile_id/version, scope, primary module/version, locale/timezone, prompt version, knowledge publication, routing/consent policy refs, enabled tool schema refs, approval actor/time; immutable after publication |
| CallSession | call_id, scope, profile snapshot, provider binding, caller lookup hint, nullable customer_id, lifecycle state, interaction epoch, last accepted sequence, active workflow refs, budgets; atomic versioned updates |
| ConversationTurn | conversation_turn_id, call_id, sequence, epoch, source event ID, final/partial flag, privacy classification; duplicates ignored and stale epochs cannot advance state |
| KnowledgeItem | knowledge_id, scope/module/audience, typed facts, source and source version, content hash, approver/time, effective/expiry times, publication/revocation state; no free-text fact silently becomes an authority |
| WorkflowInstance | workflow_id, scope, call_id, module/schema version, revision, typed module state; core stores references and revision, module validates its own state transitions |
| ActionRequest | tool_request_id, action_id, scope/call/workflow refs, tool name/schema version, typed payload hash, expected workflow revision, policy/profile refs, confirmation ref, deadline; created/persisted by server |
| ConfirmationEvidence | confirmation_id, call/turn IDs, readback/output epoch, delivery acknowledgement, action/quote hash, business-state versions, expiry, consumed-for-action ID; cannot be reused for a different request |
| ActionResult | action_id, status, typed safe output, provider operation reference, reconciliation status, audit/correlation IDs; explicit unknown outcome, never “probably submitted” |
| EscalationCase | case_id, scope/call/workflow refs, category, redacted summary, routing policy version, transfer state/callback state, employee acknowledgement; callback exists only after durable creation |

Reuse original call states: `RINGING`, `ACTIVE`, `TRANSFERRING`, `CALLBACK_REQUIRED`, `COMPLETED`, `FAILED`. Track listening/thinking/speaking and media connection/reconnection separately. Module workflows keep their own legal states; a hotel booking must not be forced into a pizza order enum. Restaurant order/submission/customer IDs from the original spec remain required within their owning domains.

## Ports and extension interfaces

The following signatures describe responsibilities. Concrete async types and event serialization will be chosen in implementation; blocking database work must not block media I/O threads.

```text
BusinessModule
  descriptor() -> ModuleDescriptor
  validateConfiguration(config, schemaVersion) -> ValidationResult
  classifySupportedIntent(observation) -> CandidateIntent | Unsupported
  reduce(workflowState, validatedInput) -> WorkflowDecision
  tools() -> List<ToolContract>
  knowledgeSchemas() -> List<FactSchema>
  evaluateDomainPolicy(action, currentFacts) -> Allow | Deny | Clarify | Escalate
  evaluationScenarios() -> TestFixtureReferences

ModuleDescriptor
  moduleId, version, compatibleCoreContractRange,
  configurationSchemaVersion, workflowSchemaVersions,
  requiredCapabilities, supportedLocales, toolSchemaVersions

WorkflowDecision
  nextTypedState, expectedRevision,
  responseEvidence | clarification | escalation,
  proposedActions[]

KnowledgePort.retrieve(trustedContext, query, audience, publicationRef)
  -> EvidenceBundle | Missing | Stale | Conflicting | Forbidden
KnowledgePort.validateEvidence(trustedContext, evidenceRefs, now)
  -> Valid | Revoked | Expired | Forbidden

DialoguePort.respond(turnContext, evidence, workflowSummary, allowedToolSchemas)
  -> ResponseProposal(text, evidenceRefs, actionProposals)

PolicyGateway.evaluate(trustedContext, persistedAction, currentFacts, confirmation)
  -> Decision(reasonCodes, requiredClarificationOrEscalation)
ActionGateway.request(trustedContext, actionProposal)
  -> Clarify | Deny | Escalate | AwaitConfirmation | Queued(actionId)

ToolContract
  namespacedName, schemaVersion, payload/result schemas,
  read/write class, identity and confirmation requirements,
  idempotency/reconciliation capabilities, timeout, sensitive-field classification

ToolAdapter.execute(internalExecutionPermit, validatedPayload, stableIdempotencyKey)
  -> Succeeded | Rejected | OutcomeUnknown
ToolAdapter.reconcile(internalExecutionPermit, providerReferenceOrStableKey)
  -> Found(result) | ProvenAbsent | Unknown

AuditPort.append(transactionContext, typedActor, event)
EscalationPort.request(trustedContext, reason, redactedSummary)
  -> PersistedCase | Unavailable
```

`trustedContext` and `internalExecutionPermit` are server-created objects inaccessible through JSON deserialization from callers/models. A permit binds tenant, principal, request hash, tool/version, policy decision, deadline, and idempotency key; it cannot authorize a different payload. Module code receives restricted interfaces, not raw datasource credentials, arbitrary HTTP clients, or the administrative session service. Adapters authenticate providers using environment-scoped secrets; provider identifiers never authorize a tenant by themselves.

Read tools pass authorization and freshness checks too. Only the gateway's executor owns adapter execution. Policy evaluation cannot itself send messages or call payment/POS endpoints. Persisted event envelopes carry tenant, schema version, correlation/causation IDs, event UUID, aggregate revision, and timestamp; workers verify their scope instead of relying on an HTTP thread-local tenant.

## Media event contract

```text
TelephonyPort: acceptVerifiedCall, receiveAudio, sendAudio, clearOutput,
               playbackAcknowledged, transferToConfiguredEmployee, close
SttPort: open, acceptAudio, partialTranscript, finalTranscript, speechStart, close
TtsPort: synthesizeStream, cancel(outputEpoch), audioChunk
DialoguePort: proposeResponse, cancel(turnEpoch)

MediaEnvelope:
  callId, providerSessionRef, eventId, sequence, epoch,
  codec/sampleRate/channels, timestamp, eventType, boundedPayload
```

Negotiate codecs and sample rate at the adapter edge. Reject invalid/oversized/out-of-order input without allowing stale events to mutate state. Keep logical call, provider connection, customer identity, and workflow IDs separate. Reconnecting transport must not silently create a second order workflow.

On caller speech during playback: increment the response epoch, clear queued output, cancel generation/synthesis, mark the previous utterance interrupted, then process the next final input once. Provider callbacks from the old epoch cannot speak or confirm anything. Keep physical playback acknowledgement distinct from “TTS finished generating.” If a provider cannot establish complete delivery of the required read-back, collect a new explicit read-back/confirmation through a supported path or escalate.

Confirmation example: customer changes a large pizza to medium while read-back is playing → invalidate the old quote/evidence → restaurant reducer creates a new revision → deterministic recalculation → deliver new complete read-back → associate the customer's final confirmation with that revision → submit once. An old “yes” or duplicate webhook cannot confirm the new quote. A change after submission never rewinds the completed effect.

## Isolation checklist for implementation tests

| Surface | Enforced scope/version behavior |
| --- | --- |
| HTTP and staff dashboard | Authenticated tenant + assigned locations; no body/header tenant override |
| Telephony | Verified account/destination routing; caller ID only a lookup hint |
| Knowledge/search/cache | Tenant/business/location/module/audience/publication filtering before ranking, recheck before use, revoke stale cache entries |
| Prompt assembly | Reviewed template + approved fact fields only; raw documents remain untrusted evidence; no secrets or other-tenant summaries |
| Workflow state | Call/workflow scoped versioned state; no static/shared conversation memory; module schema compatibility checks |
| Action queues/retries | Persisted trusted scope and stable IDs, current policy/module/kill-switch recheck, deny scope mismatch |
| Provider callbacks | Signature verification plus expected provider account/resource binding, replay protection, safe unknown-event handling |
| SSE/exports/audit | Reauthorize subscriptions and jobs; least-privilege redacted results; never trust a guessed entity ID |
| Summaries/training | Summary stays scoped and non-authoritative; never pool tenant calls for training without a separate approved data plan |

Cache invalidation, permission changes, disabled locations/modules, withdrawn consent, concurrent calls, restart, and version upgrades must be adversarial test cases. Pinning old versions is not permission to ignore revocation.

## Compatibility, failures, and evaluations

Reject unknown module IDs, tools, and schema versions. Registry enables only reviewed compiled code. Version module contracts and workflow data; let active calls drain on compatible pinned code or escalate safely. Do not hot-migrate an in-flight workflow with unreviewed conversion logic. Keep old-version readers until retained records can be interpreted safely.

Use PostgreSQL transactions/outbox for durable actions. Retry only when the adapter contract proves idempotency; a generic adapter must not promise exactly-once semantics for a provider that lacks them. Unknown final outcomes remain visible to staff and require reconciliation; a provider returning “not found” is sufficient only when its consistency guarantees establish absence. SMS failure never resubmits an order. Transfer/callback/SMS tools obey the same narrow execution model.

The reference module contains fictional opening-hours/FAQ data and callback simulation, with no POS or domain booking types. Test two tenants and later two module types against the same core binary. Restaurant conformance includes every original scenario and knowledge addendum case. Future vertical modules supply their own schemas, legal transitions, confirmation snapshots, escalation rules, adapter contracts and evaluation datasets before enablement.

This contract is internal design; leave the implemented OpenAPI unchanged until corresponding APIs exist. Do not advertise these capabilities as currently available.
