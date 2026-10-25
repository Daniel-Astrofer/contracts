# Agent guide — Kerosene Contracts

## Scope

This repository is the source of truth for cross-repository protocol schemas,
versions, compatibility rules and test vectors.

## Documentation

- Start at `docs/README.md`.
- Keep normative contracts in `docs/reference/`, repository rationale in
  `architecture/`, and deprecated inventories in `history/`.
- Services link here for shared protocols; they must not independently copy a
  schema or redefine a contract.

## Safety and compatibility

- Maintain semantic versions and a compatibility matrix.
- Breaking changes require dual-version rollout and test vectors.
- Generated Java, Rust and Dart packages derive from one source.
- Do not include secrets or environment-specific endpoints.

## Verification

Validate schemas, vectors and consumer compatibility before publishing a new
contract version.
