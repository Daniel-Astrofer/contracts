//! Canonical Kerosene discovery, membership, and admin wire contracts.
//!
//! Signatures are always calculated over [`CanonicalSignable::signing_bytes`],
//! never over an arbitrary JSON serialization.
//!
//! Cross-language signing uses canonical JSON
//! ([`canonical_json_bytes`]) which produces the same bytes in Rust and Java.

pub mod admin;
pub mod canonical;
pub mod discovery;
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
