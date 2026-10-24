//! Canonical Rust wire contracts shared across Kerosene services.
//!
//! The crate defines administrative, discovery and membership, and financial
//! ledger messages. Discovery contracts also carry member identity and trust-root
//! data used by the membership protocols.
//!
//! Signatures are always calculated over [`CanonicalSignable::signing_bytes`],
//! never over an arbitrary JSON serialization.
//!
//! Cross-language JSON serialization uses [`canonical_json_bytes`] so Rust and
//! Java produce the same compact representation with sorted object keys.

/// Administrative status, audit, provider, and reconciliation wire contracts.
pub mod admin;

/// Canonical JSON serialization, hashing, and deterministic signing encodings.
pub mod canonical;

/// Peer discovery, member identity, trust, and membership wire contracts.
pub mod discovery;

/// Financial account snapshots and ledger journal wire contracts.
pub mod ledger;

// ---------------------------------------------------------------------------
// Re-exports for root-level wire compatibility
// ---------------------------------------------------------------------------

pub use admin::{
    AdminErrorEnvelopeV1, AdminOnrampV1, AdminP2PV1, AdminProviderV1, AdminReconciliationV1,
    AuditReferenceV1, NodeAdminStatusV1, VaultAdminStatusV1, ADMIN_AUDIT_REFERENCE_DOMAIN,
    ADMIN_CONTRACT_VERSION, ADMIN_ERROR_ENVELOPE_DOMAIN, ADMIN_NODE_STATUS_DOMAIN,
    ADMIN_ONRAMP_DOMAIN, ADMIN_P2P_DOMAIN, ADMIN_PROVIDER_DOMAIN, ADMIN_RECONCILIATION_DOMAIN,
    ADMIN_VAULT_STATUS_DOMAIN,
};

pub use canonical::{canonical_hash, canonical_json_bytes, canonical_json_hash, CanonicalSignable};

pub use discovery::{
    member_id, AdmissionRequestV1, DiscoveryPlane, GenesisTrustBundleV1, ManifestMember,
    ManifestSignature, MembershipManifestV1, MembershipPhase, PeerHelloV1, TrustMember, TrustPlane,
    ADMISSION_REQUEST_DOMAIN, DISCOVERY_CONTRACT_VERSION, GENESIS_TRUST_BUNDLE_DOMAIN,
    MEMBERSHIP_MANIFEST_DOMAIN, PEER_HELLO_DOMAIN,
};

pub use ledger::{
    JournalDirection, LedgerAccountV1, LedgerJournalV1, ADMIN_CORE_LEDGER_ACCOUNT_DOMAIN,
    ADMIN_CORE_LEDGER_JOURNAL_DOMAIN,
};
