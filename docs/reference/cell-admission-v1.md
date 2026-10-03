# Initial Cell admission v1 (implementation in progress)

The Rust `CellAdmissionV1` payload is an additive initial-install intent. It is
not a release approval, an ordered consensus certificate, proof of compatibility,
or permission to activate signers. No installer currently consumes it.

Canonical hashing follows the existing release JSON rules. Every field must be
covered by quorum signatures: schema, governance network/epoch, Cell identity,
cluster UID, release approval digest, operator/change attribution, nonce and time.
The cluster UID denotes the live `kube-system` Namespace UID, not a caller-chosen
label. Recreating or cloning that namespace requires independent identity review.

Time fields are exact integer UTC Unix seconds (`issuedAtUnixSeconds` and
`expiresAtUnixSeconds`), not timestamps interpreted differently by consumers.
Validity is half-open: issued <= now < expires, with at most 3600 seconds lifetime.
No implicit clock skew extends authorization. Epoch is 1 through 2^53-1.
Nonce is 32 random bytes encoded as 64 lowercase hexadecimal characters.

Typed validation and digest computation establish only shape and time. The
consumer must independently verify pinned Bank quorum signatures, verify the
referenced approval via existing consensus proof, authenticate the operator,
recheck the bound cluster, and atomically consume the nonce in an authoritative
durable registry before effects. A local file does not prevent cross-host replay.
Failures must retain consumption and immutable operation identity for explicit
recovery; deleting an installation must not reset admission history.

The envelope is exactly `{admission, signatures}`; signature entries are exactly
`{memberId, signatureBase64}`. No caller-supplied public key or threshold is
accepted. Signatures cover canonical admission payload bytes, excluding the
envelope. Structure validation rejects duplicate members and requires canonical
padded Base64 encoding of 64 bytes, with 1 through 64 entries. It deliberately
does not establish membership, signature validity or quorum: even a zero-byte
signature can pass shape checks. Independently pinned policy controls those checks.

Generated payload/envelope schemas are in `schemas/release/`. Regenerate from
the Rust source with `cargo run --locked -p kerosene-contracts --example
release_schemas -- --write`; verify with `--check`. Schemas enforce field shape,
not duplicate member identities, time ordering/expiry or stateful authorization;
the typed validator and consumer must enforce those additional rules.

`test-vectors/cell-admission-v1.json` contains publicly known synthetic identities,
three signatures, canonical payload bytes and digest. Regenerate/verify with
the `cell_admission_fixture` example's `--write`/`--check` flags. It is never
operational approval. Deploy's Go verifier consumes this exact Contracts-owned
file in its opt-in interoperability test; no copied schema/vector is authoritative.

Producer, authoritative replay
registry and deploy integration are pending. Do not publish or enable this
contract as a usable authorization mechanism until those are implemented and
verified. Existing release approval v1 and consensus state hashing are unchanged.
