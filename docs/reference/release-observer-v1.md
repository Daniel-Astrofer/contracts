# Release observer contract 1.0.0 (contracts package 0.3.0)

Normative source: `rust/kerosene-contracts/src/release.rs`. JSON schemas in
`schemas/release/` are generated from this source. Reproduce with
`cargo run -p kerosene-contracts --example release_schemas`; verify committed
schemas with the same command followed by `-- --check`. No Gradle is needed.
All JSON fields use camelCase. Unknown fields are rejected.

## Node API for operator and Core agents

All routes are on Node's existing mandatory client-certificate mTLS listener.
The observer must be configured on the Bank plane. Missing configuration returns
503 `{ "code": "observer_unconfigured" }`. A client certificate authenticates
transport access; it cannot assert compatibility. Private keys and endpoints
come from operator configuration, never request bodies.

* `GET /v1/release-observer/discover` returns schema
  `kerosene.release-observer-discovery/v1`, `observerId`, `networkId`,
  `publicKeyDerBase64`, `bankObservers`, `reportSchema`,
  `maxReportLifetimeSeconds`, and `commitCertificate: "unavailable"`.
* `POST /v1/release-observer/verify` consumes `ReleaseObserverRequestV1`
  below. Node queries **every** listed observer's configured Bank source,
  validates its pinned signature and fresh challenge, and durably caches the
  signed reads, including authenticated `incompatible`/`unknown` observations
  so operators can see blockers. It returns `ReleaseObservationsV1` below. It does not reserve
  the target for aggregate report signing.
* `GET /v1/releases/observations?releaseDigest=sha256:<64 lowercase hex>`
  returns the cached `ReleaseObservationsV1`. Encode the query value using the
  HTTP client's query builder. Only the most recently verified target is cached
  per network. Unknown digests return 404; expired reads return 410. This is an
  unsigned retrieval wrapper containing individually signed observations.
* `POST /v1/release-observer/sign` consumes the identical request, independently
  repeats the Bank reads, reserves the target sequence durably, and returns a
  `BankObserverReportV2` with one signature by this Node observer. Submit the
  **identical unsigned report** to each required observer; combine their unique
  `signatures` arrays without changing any other field. Deploy still requires
  quorum signatures over the exact full aggregate document.

Request body (the `report` has **no signatures field**):

```json
{
  "releaseLock": {"schema":"kerosene.release-lock/v3","schemaVersion":3,"releaseId":"release-001","network":{"id":"bank-main","plane":"bank"},"sequence":1},
  "report": {
    "schema":"kerosene.bank-observer-report/v2",
    "releaseId":"release-001",
    "networkId":"bank-main",
    "targetSequence":1,
    "releaseLockCanonicalDigest":"sha256:<canonical releaseLock hash>",
    "issuedAt":"2026-10-01T21:00:00Z",
    "expiresAt":"2026-10-01T21:05:00Z",
    "observations":[{
      "observerId":"bank-001","status":"compatible","observedSequence":1,
      "releaseDigest":"sha256:<same canonical releaseLock hash>",
      "observedAt":"2026-10-01T21:00:00Z"
    }]
  }
}
```

The real releaseLock must be supplied in full. The minimal object above shows
the identity fields Node binds; Node requires the matching v1/v2/v3 schema tag
and version and a Bank plane, and is not a replacement for deploy release-lock,
TUF or policy validation. A status in the request is only a claim to check:
Node will not sign it unless the authoritative Bank read agrees. Every observer
must be configured as a pinned Bank source; the local observer must be present.
For aggregate **signing**, the local signer must appear as a compatible
observation; negative or unknown local reads cannot authorize an update.
Duplicate observer IDs are rejected. Bank timestamps
may differ from the proposed observation timestamps; both must independently
pass the age gates. Compatible sequence and digest must match the target.

Response wrapper:

```text
ReleaseObservationsV1 {
  schema: "kerosene.release-observations/v1",
  observations: [SignedReleaseObservationV1, ...]
}
SignedReleaseObservationV1 {
  schema: "kerosene.signed-release-observation/v1",
  releaseId, networkId, targetSequence, releaseLockCanonicalDigest,
  issuedAt, expiresAt, observation: BankObservationV2,
  bankRead: BankReleaseReadV1,
  commitCertificate: "unavailable",
  signatures: [{ memberId, publicKeyDerBase64, signatureBase64 }]
}
```

Each read wrapper is signed by the serving Node's observer key. The embedded
Bank read is independently signed by that Bank source's pinned key. For reads
from other Banks the wrapper signer remains the serving Node; it is **not** an
additional Bank or quorum vote. Individually signed observation documents
cannot be converted into aggregate report signatures.

## Exact Bank API to implement in Core

Node performs an authenticated mTLS **read**, with redirects disabled:

`GET /v1/releases/observation?releaseDigest=sha256:<digest>&challenge=<64 lowercase hex>`

The base URL, trusted server CA, client identity and Ed25519 Bank SPKI public
key are operator configured. Core must require a verified client certificate.
Core must look up the actual release target and derive the observation from
its own runtime compatibility/evidence checks, never echo a caller's status.
Unknown, unsupported or incompatible targets must not return compatible.
Absent ordered governance is distinct from compatibility and must not create
a commit certificate. Synthetic adapters must use `source: "synthetic"`, which
Node rejects even with a valid signature.

The response is `BankReleaseReadV1`, HTTP 200 JSON:

```text
{
  schema: "kerosene.bank-release-read/v1",
  releaseId, networkId, targetSequence, releaseLockCanonicalDigest,
  issuedAt, expiresAt,
  challenge: <exact request challenge>,
  source: "bank-runtime",
  observation: {
    observerId, status: "compatible" | "incompatible" | "unknown",
    observedSequence, releaseDigest, observedAt
  },
  signatures: [{ memberId: <same observerId>, publicKeyDerBase64, signatureBase64 }]
}
```

Exactly one Bank signature is accepted. It signs canonical JSON of the whole
response **minus only `signatures`**. A fresh cryptographically random 256-bit
challenge is generated for each outbound read. It binds the entire signed Bank
response and is not accepted from the Node caller. Bank reads expire at most
60 seconds after issue, may have at most 5 seconds future clock skew, and may be
no more than 60 seconds old at verification. Their observation may be at most
15 minutes older than issue and at most 5 seconds later. Non-2xx, redirects,
timeouts, oversized bodies, missing certificates and bad signatures fail closed.

## Canonicalization and limits

Deploy compatibility source is the v2 Bank observer report schema and
`verify_bank_observer_report` in `deploy/infra/kerosene-stack`. Preserve its
exact field set and signature representation. Canonical signing bytes are
compact UTF-8 JSON with lexicographically sorted object keys recursively,
unescaped Unicode and preserved array order. There is **no domain prefix**
for v2 report signatures. `releaseLockCanonicalDigest` is `sha256:` plus the
lowercase SHA-256 of the full canonical releaseLock. Integers must be in the
exact interoperable range (absolute value at most 2^53-1); floats are rejected.
Do not hash pretty-printed JSON, raw file bytes, or a signing digest instead
of signing the canonical payload bytes. Public keys are standard Base64 of
Ed25519 X.509 SubjectPublicKeyInfo DER; signatures are standard Base64 of the
64 raw Ed25519 signature bytes. Trust comes from an external pinned roster,
never an included public key alone.

IDs match `^[a-z0-9][a-z0-9._-]{2,127}$`. Digests match
`^sha256:[0-9a-f]{64}$`. Target sequences are 1 through 2^53-1. The request
body and each Bank response are limited to 262144 bytes; there are at most
64 observers, at most four concurrent verification operations, a 3 second
connect timeout, 5 second per-Bank timeout and 10 second overall read deadline.
Report issue may be at most five minutes in the future; expiration must be
after issue and current time, at most one hour after issue. Proposed report
observations may be at most fifteen minutes older or five minutes later than
issue. Verify again after network IO before signing. Read wrappers expire with
their embedded Bank evidence, much earlier than the allowed report lifetime.

Error envelopes are `{ "code": "..." }`: 409 `sequence_replay`; 404
`observations_unknown`; 410 `evidence_expired`; 422 `report_invalid`,
`bank_evidence_invalid`, `bank_incompatible`; 502 `bank_unavailable`; 429
`observer_busy`; 503 `observer_unconfigured`, `observer_persistence_failed`.
Malformed JSON, duplicate fields (including within releaseLock), and invalid
typed request fields return 422 `report_invalid`. Non-JSON content types return
415 `report_invalid`; malformed query parameters use Axum's 400 rejection.
Oversized requests return 413. No signatures are returned on error.

## Persistence and consensus boundary

Signing sequences strictly increase per network. Equal sequences, lower
sequences and a different digest at an already signed sequence are rejected,
including after process restart and concurrent requests. The reservation and
payload digest are flushed before signature exposure. A crash in between
burns the sequence. Failed Bank verification does not consume a signing
sequence. Do not delete/reset observer state to retry an old sequence.

This is a signed compatibility observation service with local anti-replay
state. **It provides no BFT ordering, financial ledger finality or actual
release commit certificate.** `commitCertificate` is explicitly unavailable.
Deploy's existing signature quorum check remains a separate aggregation gate;
it must not be interpreted as proof of consensus. A separate governance ADR
in Node proposes bounded ordered release governance without ledger changes.

## Compatibility matrix

| Artifact | Producer | Consumer | Rollout |
| --- | --- | --- | --- |
| contracts 0.3.0 release module | Contracts | Node, Core | Additive to discovery/admin 0.1/0.2 |
| bank-observer-report/v2 | Node / operator aggregate | Existing deploy verifier | Exact v2 shape preserved |
| bank-release-read/v1 | Core Bank | Node | Configure Core producer before enabling observer |
| release-observations/v1 | Node | Operator / Core | New authenticated read API |

Rust consumers import `kerosene_contracts::release`. Other languages consume
the generated JSON schemas; no independent Java/Dart schema is introduced and
no Java build is required for this release module. Fixture keys and signatures
are public deterministic test vectors, not operational credentials.
