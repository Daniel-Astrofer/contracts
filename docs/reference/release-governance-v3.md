# Release lock v3 and release approval v1

Normative contract source: `rust/kerosene-contracts/src/release.rs`.
Generated artifacts: `schemas/release/release-lock-v3.schema.json` and
`schemas/release/release-approval-v1.schema.json`. Consumers must validate
both the schema and the stateful rules below. Schema generation and checking
use the Rust `release_schemas` example; no Gradle or publication is required.

`kerosene.release-lock/v3` has `schemaVersion: 3` and preserves the v2 release
descriptor: `releaseId`, `sequence`, `network: {id,plane:"bank"}`, source,
services, policy, migration, detached TUF authorization, and Vault compatibility.
The **only** BFT authorization fields inside the lock are:

```json
{"networkId":"bank-release-governance","epoch":1,"threshold":3,"members":4}
```

`height` and `commitDigest` are forbidden, including alongside those fields.
The immutable lock is created before ordering; embedding the commit digest
would create the circular dependency lock digest → approval transaction →
block hash → lock digest. TUF remains `{targetPath}`; target length, digest,
versions and expiry live in detached signed metadata, as in v2. The BFT
network ID can differ from the Cell's `network.id`; approval transactions bind
the governance ID in `authorization.bft.networkId`. Observer reports bind the
Cell network ID in `network.id` and hash the **entire** v3 lock unchanged.

Epoch and sequence are integers from 1 through 2^53-1. Membership is 4 through
64. Threshold must be greater than two thirds of declared members and no more
than members; minimumBankObservers must be at least threshold and no more
than members. Deployment policy cannot allow source builds or Vault signer
activation. Existing service/repository mapping, snapshot and recovery gates
remain enforced by deploy in addition to the schema.

The separately ordered approval transaction is exactly:

```json
{
  "schema":"kerosene.release-approval/v1",
  "networkId":"bank-release-governance",
  "epoch":1,
  "sequence":42,
  "releaseLockCanonicalDigest":"sha256:<full canonical v3 lock hash>",
  "previousApprovalDigest":"sha256:<canonical previous approval tx hash>"
}
```

No additional fields, signatures, heights or block hashes belong to the
transaction. Canonical bytes follow the release observer specification:
sorted compact UTF-8 JSON, preserved array order, exact-range integers,
SHA-256 digest prefixed with `sha256:`. `ReleaseApprovalV1::digest()` returns
the canonical transaction digest. The first accepted approval in a newly
initialized governance chain uses `sha256:` plus 64 zeroes as the predecessor
sentinel and sequence 1. Subsequent approvals must link to the actual previous
accepted approval digest and use exactly the previous sequence plus one; gaps
are rejected by the initial Go ABCI application. Epoch must equal the static
authorized governance policy epoch. Implicit epoch/membership rotations are
forbidden in the initial verifier. Any future rotation requires a separately
versioned protocol and authorization rather than silently changing the
persisted policy. Initialization, duplicate/fork rejection, and persistence
are consensus application responsibilities.

Actual consensus proof is **detached**: light blocks and approval transaction
inclusion must be verified against a separately trusted anchor and validator
set using the consensus implementation's pinned protocol. A proof must bind
governance network, epoch, canonical lock digest, exact approval transaction,
block/header hash, verified validator voting power and actual commit evidence.
It must also prove the application accepted this transaction, rather than only
showing arbitrary bytes in a block. Trust must not originate solely in a
validator set bundled with the supplied proof. Anchor trust period, validator
transitions, proof limits and freshness must be enforced by that verifier.
The separate `deploy/infra/governance` integration supplies the Go ABCI
application and offline CometBFT proof verifier. Its bounded initial policy is
a separately trusted static anchor of four validators with equal voting power
and a pinned governance epoch. Proof light blocks are contiguous from that
anchor; the approval transaction is included in DataHash at height H, and the
complete approvals stateHash is proven by AppHash at H+1. Genuine CometBFT
precommits, including at least three of the four validators, are required.
Consumers must use that verifier's exact state hashing/inclusion rules and
backend wire proof schema, not independently invent another encoding.
Node's observer certificate remains unavailable: it has not integrated this
proof verifier and cannot claim that an observation was ordered.

Detached signatures in `kerosene.release-receipt/v1` are **not consensus proof**.
Keep receipt/report v1/v2 compatibility only where explicitly required by a
dual-version rollout; never relabel their signature threshold as a committed
v3 approval. The existing Bank report remains `bank-observer-report/v2` with
exact full-document quorum signatures. Individually signed read observations
remain read evidence only. Node returns commit certificate unavailable until
the separate consensus proof integration is implemented.

| Lock | TUF binding | BFT binding | Rollout |
| --- | --- | --- | --- |
| v1 | Embedded legacy metadata fields | Embedded legacy height/digest | Existing consumers only |
| v2 | Detached TUF metadata | Embedded legacy height/digest | Existing deploy compatibility |
| v3 | Detached TUF metadata | Epoch/membership in lock; detached consensus proof | Deploy parser and Go ABCI/offline verifier |

Node observer target extraction uses `releaseId`, `sequence`, and `network.id`
across all three versions. It does not remove authorization fields before
hashing. Main coordination owns deploy parser and the separate Go ABCI module;
this Contracts/Node change does not alter those repositories or financial
ledger behavior.
