use kerosene_contracts::*;
use sha2::{Digest, Sha256};

// ------------------------------------------------------------------
// Legacy discovery tests
// ------------------------------------------------------------------

#[test]
fn member_id_is_network_bound() {
    let key = [7_u8; 32];
    assert_ne!(member_id("testnet", &key), member_id("mainnet", &key));
    assert_eq!(member_id("testnet", &key).len(), 64);
}

#[test]
fn signatures_do_not_change_manifest_hash() {
    let mut manifest = MembershipManifestV1 {
        contract_version: DISCOVERY_CONTRACT_VERSION.into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Vault,
        epoch: 1,
        phase: MembershipPhase::Stable,
        previous_manifest_hash: "0".repeat(64),
        threshold: 2,
        members: vec![],
        next_epoch: None,
        signatures: vec![],
    };
    let unsigned = canonical_hash(&manifest);
    manifest.signatures.push(ManifestSignature {
        signer_id: "member-a".into(),
        signature: "1".repeat(128),
    });
    assert_eq!(unsigned, canonical_hash(&manifest));
}

#[test]
fn canonical_bytes_are_not_ambiguous() {
    let mut left = PeerHelloV1 {
        contract_version: DISCOVERY_CONTRACT_VERSION.into(),
        network_id: "ab".into(),
        plane: DiscoveryPlane::Bank,
        member_id: "c".into(),
        root_public_key: "1".repeat(64),
        challenge: "2".repeat(64),
        issued_at_epoch_ms: 1,
        endpoint: "https://example.onion".into(),
        signature: String::new(),
    };
    let mut right = left.clone();
    right.network_id = "a".into();
    right.member_id = "bc".into();
    assert_ne!(left.signing_bytes(), right.signing_bytes());
    left.signature = "f".repeat(128);
    assert_eq!(left.signing_bytes(), {
        right.network_id = "ab".into();
        right.member_id = "c".into();
        right.signing_bytes()
    });
}

// ------------------------------------------------------------------
// Admin unknown-field rejection tests
// ------------------------------------------------------------------

#[test]
fn admin_node_status_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "network_id":"kerosene-test",
        "plane":"bank",
        "local_ready":true,
        "member_ready":true,
        "quorum_ready":false,
        "financial_ready":false,
        "live_members":1,
        "threshold":2,
        "secret":"must-not-pass"
    }"#;
    assert!(serde_json::from_str::<NodeAdminStatusV1>(payload).is_err());
}

#[test]
fn admin_vault_status_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "local_ready":true,
        "financial_ready":true,
        "node_id":"node-1",
        "ceremony_mode":"production",
        "bitcoin_network":"mainnet",
        "unknown_field":"xyz"
    }"#;
    assert!(serde_json::from_str::<VaultAdminStatusV1>(payload).is_err());
}

#[test]
fn admin_error_envelope_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "code":"ERR_TEST",
        "message":"test error",
        "request_id":"req-1",
        "details":{},
        "extra":"should-fail"
    }"#;
    assert!(serde_json::from_str::<AdminErrorEnvelopeV1>(payload).is_err());
}

#[test]
fn admin_audit_reference_rejects_unknown_fields() {
    let payload = r#"{
        "event_id":"evt-1",
        "request_id":"req-1",
        "occurred_at":"2026-07-30T12:00:00Z",
        "bogus":null
    }"#;
    assert!(serde_json::from_str::<AuditReferenceV1>(payload).is_err());
}

// ------------------------------------------------------------------
// Multi-saldo ledger tests
// ------------------------------------------------------------------

#[test]
fn ledger_account_serde_roundtrip() {
    let account = LedgerAccountV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        account_id: "acc-123".into(),
        account_type: "checking".into(),
        available_sats: 800_000_000,
        reserved_sats: 100_000_000,
        pending_incoming_sats: 50_000_000,
        pending_outgoing_sats: 20_000_000,
        confirmed_onchain_sats: 500_000_000,
        unconfirmed_onchain_sats: 10_000_000,
        spendable_by_kerosene_sats: 700_000_000,
        state_version: 42,
        state_root: "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2".into(),
        tags: vec!["hot".into(), "operational".into()],
        created_at: "2026-01-01T00:00:00Z".into(),
        updated_at: "2026-07-01T00:00:00Z".into(),
    };
    let json = serde_json::to_string(&account).unwrap();
    let deserialized: LedgerAccountV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(account, deserialized);
}

#[test]
fn ledger_account_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "account_id":"acc-1",
        "account_type":"savings",
        "available_sats":500000000,
        "reserved_sats":0,
        "pending_incoming_sats":0,
        "pending_outgoing_sats":0,
        "confirmed_onchain_sats":500000000,
        "unconfirmed_onchain_sats":0,
        "spendable_by_kerosene_sats":500000000,
        "state_version":1,
        "state_root":"abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
        "tags":[],
        "created_at":"2026-01-01T00:00:00Z",
        "updated_at":"2026-07-01T00:00:00Z",
        "unknown":"rejected"
    }"#;
    assert!(serde_json::from_str::<LedgerAccountV1>(payload).is_err());
}

#[test]
fn ledger_account_has_no_legacy_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "account_id":"acc-1",
        "account_type":"savings",
        "balance":"500000",
        "currency":"BTC",
        "tags":[],
        "created_at":"2026-01-01T00:00:00Z",
        "updated_at":"2026-07-01T00:00:00Z"
    }"#;
    assert!(
        serde_json::from_str::<LedgerAccountV1>(payload).is_err(),
        "legacy balance/currency fields must be rejected"
    );
}

#[test]
fn ledger_journal_serde_roundtrip() {
    let entry = LedgerJournalV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        entry_id: "entry-001".into(),
        account_id: "acc-123".into(),
        direction: JournalDirection::Debit,
        amount_sats: 50_000,
        description: "Initial deposit".into(),
        reference: "ref-tx-001".into(),
        recorded_at: "2026-07-30T12:00:00Z".into(),
    };
    let json = serde_json::to_string(&entry).unwrap();
    let deserialized: LedgerJournalV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(entry, deserialized);
}

#[test]
fn ledger_journal_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "entry_id":"entry-1",
        "account_id":"acc-1",
        "direction":"debit",
        "amount_sats":100,
        "description":"test",
        "reference":"ref-1",
        "recorded_at":"2026-07-30T12:00:00Z",
        "invalid":"rejected"
    }"#;
    assert!(serde_json::from_str::<LedgerJournalV1>(payload).is_err());
}

#[test]
fn ledger_journal_has_no_legacy_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "entry_id":"entry-1",
        "account_id":"acc-1",
        "direction":"debit",
        "amount":"100",
        "currency":"BTC",
        "description":"test",
        "reference":"ref-1",
        "recorded_at":"2026-07-30T12:00:00Z"
    }"#;
    assert!(
        serde_json::from_str::<LedgerJournalV1>(payload).is_err(),
        "legacy amount/currency fields must be rejected"
    );
}

#[test]
fn journal_direction_serialization() {
    assert_eq!(
        serde_json::to_string(&JournalDirection::Debit).unwrap(),
        "\"debit\""
    );
    assert_eq!(
        serde_json::to_string(&JournalDirection::Credit).unwrap(),
        "\"credit\""
    );
}

// ------------------------------------------------------------------
// Canonical JSON tests
// ------------------------------------------------------------------

#[test]
fn canonical_json_is_deterministic() {
    let node = NodeAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-1".into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Bank,
        local_ready: true,
        member_ready: false,
        quorum_ready: false,
        financial_ready: true,
        live_members: 3,
        threshold: 2,
    };
    let bytes1 = canonical_json_bytes(&node);
    let bytes2 = canonical_json_bytes(&node);
    assert_eq!(bytes1, bytes2);
}

#[test]
fn canonical_json_sorts_keys() {
    let node = NodeAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-1".into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Bank,
        local_ready: true,
        member_ready: false,
        quorum_ready: false,
        financial_ready: true,
        live_members: 3,
        threshold: 2,
    };
    let json_str = String::from_utf8(canonical_json_bytes(&node)).unwrap();
    let cv_pos = json_str.find("\"contract_version\"").unwrap();
    let fr_pos = json_str.find("\"financial_ready\"").unwrap();
    assert!(
        cv_pos < fr_pos,
        "keys must be sorted: contract_version before financial_ready"
    );
}

#[test]
fn canonical_json_hash_is_stable() {
    let node = NodeAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-canonical".into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Vault,
        local_ready: true,
        member_ready: true,
        quorum_ready: true,
        financial_ready: false,
        live_members: 5,
        threshold: 3,
    };
    let hash1 = canonical_json_hash(&node);
    let hash2 = canonical_json_hash(&node);
    assert_eq!(hash1, hash2);
    assert_eq!(hash1.len(), 64);
}

#[test]
fn canonical_json_and_binary_hash_differ() {
    let node = NodeAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-dual".into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Vault,
        local_ready: true,
        member_ready: true,
        quorum_ready: true,
        financial_ready: false,
        live_members: 5,
        threshold: 3,
    };
    let json_hash = canonical_json_hash(&node);
    let binary_hash = canonical_hash(&node);
    assert_ne!(
        json_hash, binary_hash,
        "canonical JSON and binary hashes must differ"
    );
}

// ------------------------------------------------------------------
// Binary canonical hash stability tests
// ------------------------------------------------------------------

#[test]
fn admin_canonical_hash_is_stable() {
    let node = NodeAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-canonical".into(),
        network_id: "kerosene-test".into(),
        plane: DiscoveryPlane::Vault,
        local_ready: true,
        member_ready: true,
        quorum_ready: true,
        financial_ready: false,
        live_members: 5,
        threshold: 3,
    };
    let hash1 = canonical_hash(&node);
    let hash2 = canonical_hash(&node);
    assert_eq!(hash1, hash2);
    assert_eq!(hash1.len(), 64);
}

#[test]
fn vault_admin_canonical_hash() {
    let vault = VaultAdminStatusV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-vault".into(),
        local_ready: true,
        financial_ready: true,
        node_id: "vault-node-1".into(),
        ceremony_mode: "production".into(),
        bitcoin_network: "mainnet".into(),
    };
    let hash = canonical_hash(&vault);
    assert_eq!(hash.len(), 64);
}

#[test]
fn ledger_journal_canonical_hash() {
    let entry = LedgerJournalV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        entry_id: "entry-canon".into(),
        account_id: "acc-456".into(),
        direction: JournalDirection::Credit,
        amount_sats: 250_000,
        description: "Settlement".into(),
        reference: "ref-settle-001".into(),
        recorded_at: "2026-07-29T18:30:00Z".into(),
    };
    let hash = canonical_hash(&entry);
    assert_eq!(hash.len(), 64);
}

#[test]
fn ledger_account_canonical_hash() {
    let account = LedgerAccountV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        account_id: "acc-hash-001".into(),
        account_type: "checking".into(),
        available_sats: 800_000_000,
        reserved_sats: 100_000_000,
        pending_incoming_sats: 50_000_000,
        pending_outgoing_sats: 20_000_000,
        confirmed_onchain_sats: 500_000_000,
        unconfirmed_onchain_sats: 10_000_000,
        spendable_by_kerosene_sats: 700_000_000,
        state_version: 42,
        state_root: "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2".into(),
        tags: vec!["hot".into(), "operational".into()],
        created_at: "2026-01-01T00:00:00Z".into(),
        updated_at: "2026-07-01T00:00:00Z".into(),
    };
    let hash = canonical_hash(&account);
    assert_eq!(hash.len(), 64);
}

// ------------------------------------------------------------------
// Missing contract serialization tests
// ------------------------------------------------------------------

#[test]
fn admin_p2p_serde_roundtrip() {
    let p2p = AdminP2PV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-p2p-001".into(),
        channel_id: "chan-001".into(),
        remote_node_id: "node-9876".into(),
        capacity_sats: 10_000_000,
        local_balance_sats: 6_000_000,
        remote_balance_sats: 4_000_000,
        is_active: true,
    };
    let json = serde_json::to_string(&p2p).unwrap();
    let deserialized: AdminP2PV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(p2p, deserialized);
}

#[test]
fn admin_onramp_serde_roundtrip() {
    let onramp = AdminOnrampV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-onramp-001".into(),
        order_id: "order-abc".into(),
        user_id: "user-xyz".into(),
        fiat_currency: "USD".into(),
        fiat_amount: "100.00".into(),
        sats: 1_000_000,
        provider: "stripe".into(),
        status: "completed".into(),
        created_at: "2026-07-30T12:00:00Z".into(),
    };
    let json = serde_json::to_string(&onramp).unwrap();
    let deserialized: AdminOnrampV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(onramp, deserialized);
}

#[test]
fn admin_reconciliation_serde_roundtrip() {
    let rec = AdminReconciliationV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-rec-001".into(),
        reconciliation_id: "rec-001".into(),
        ledger_sats: 1_000_000_000,
        onchain_sats: 999_800_000,
        lightning_sats: 200_000,
        delta_sats: -200_000,
        status: "settled".into(),
        reconciled_at: "2026-07-30T12:00:00Z".into(),
    };
    let json = serde_json::to_string(&rec).unwrap();
    let deserialized: AdminReconciliationV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(rec, deserialized);
}

#[test]
fn admin_provider_serde_roundtrip() {
    let prov = AdminProviderV1 {
        contract_version: ADMIN_CONTRACT_VERSION.into(),
        request_id: "req-prov-001".into(),
        provider_id: "lnd-mainnet".into(),
        provider_type: "lightning".into(),
        is_online: true,
        last_heartbeat: "2026-07-30T12:00:00Z".into(),
        version: "0.18.3".into(),
    };
    let json = serde_json::to_string(&prov).unwrap();
    let deserialized: AdminProviderV1 = serde_json::from_str(&json).unwrap();
    assert_eq!(prov, deserialized);
}

#[test]
fn admin_p2p_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "channel_id":"chan-1",
        "remote_node_id":"node-1",
        "capacity_sats":10000000,
        "local_balance_sats":6000000,
        "remote_balance_sats":4000000,
        "is_active":true,
        "bogus":true
    }"#;
    assert!(serde_json::from_str::<AdminP2PV1>(payload).is_err());
}

#[test]
fn admin_onramp_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "order_id":"order-1",
        "user_id":"user-1",
        "fiat_currency":"USD",
        "fiat_amount":"100.00",
        "sats":1000000,
        "provider":"stripe",
        "status":"pending",
        "created_at":"2026-07-30T12:00:00Z",
        "extra":"nope"
    }"#;
    assert!(serde_json::from_str::<AdminOnrampV1>(payload).is_err());
}

#[test]
fn admin_reconciliation_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "reconciliation_id":"rec-1",
        "ledger_sats":1000000000,
        "onchain_sats":999800000,
        "lightning_sats":200000,
        "delta_sats":-200000,
        "status":"settled",
        "reconciled_at":"2026-07-30T12:00:00Z",
        "unknown":"reject"
    }"#;
    assert!(serde_json::from_str::<AdminReconciliationV1>(payload).is_err());
}

#[test]
fn admin_provider_rejects_unknown_fields() {
    let payload = r#"{
        "contract_version":"0.1.0",
        "request_id":"req-1",
        "provider_id":"lnd-mainnet",
        "provider_type":"lightning",
        "is_online":true,
        "last_heartbeat":"2026-07-30T12:00:00Z",
        "version":"0.18.3",
        "invalid":"reject"
    }"#;
    assert!(serde_json::from_str::<AdminProviderV1>(payload).is_err());
}

// ------------------------------------------------------------------
// KAT — Known Answer Tests (cross-language with Java)
// ------------------------------------------------------------------

fn load_json(path: &str) -> serde_json::Value {
    let content = std::fs::read_to_string(path)
        .unwrap_or_else(|e| panic!("cannot read test vector {path}: {e}"));
    serde_json::from_str(&content).unwrap()
}

fn hex_hash(data: &[u8]) -> String {
    hex::encode(Sha256::digest(data))
}

fn strip_vector_meta(mut json: serde_json::Value) -> serde_json::Value {
    if let Some(obj) = json.as_object_mut() {
        obj.remove("vector_label");
        obj.remove("expected_json_hash");
        obj.remove("expected_binary_hash");
    }
    json
}

fn kat_canonical_json<T>(json: &serde_json::Value, expected_hash: &str)
where
    T: serde::de::DeserializeOwned + serde::Serialize + std::fmt::Debug,
{
    let stripped = strip_vector_meta(json.clone());
    let obj: T =
        serde_json::from_value(stripped).unwrap_or_else(|e| panic!("KAT deserialize failed: {e}"));
    let canonical = canonical_json_bytes(&obj);
    let actual = hex_hash(&canonical);
    assert_eq!(
        actual,
        expected_hash,
        "KAT canonical JSON hash mismatch for {}",
        std::any::type_name::<T>(),
    );
}

fn kat_binary_signable<T>(json: &serde_json::Value, expected_hash: &str)
where
    T: serde::de::DeserializeOwned + CanonicalSignable + std::fmt::Debug,
{
    let stripped = strip_vector_meta(json.clone());
    let obj: T =
        serde_json::from_value(stripped).unwrap_or_else(|e| panic!("KAT deserialize failed: {e}"));
    let actual = canonical_hash(&obj);
    assert_eq!(
        actual,
        expected_hash,
        "KAT binary hash mismatch for {}",
        std::any::type_name::<T>(),
    );
}

fn vectors_dir() -> String {
    let candidates: Vec<&str> = vec!["../../test-vectors", "test-vectors", "../test-vectors"];
    for c in &candidates {
        if std::path::Path::new(c).exists() {
            return c.to_string();
        }
    }
    panic!("cannot find test-vectors directory (tried: {candidates:?})");
}

macro_rules! kat_test {
    ($name:ident, $ty:ty, $fname:expr, $hash_field:expr) => {
        #[test]
        fn $name() {
            let dir = vectors_dir();
            let path = std::path::Path::new(&dir).join($fname);
            let json = load_json(path.to_str().unwrap());
            let expected = json[$hash_field]
                .as_str()
                .unwrap_or_else(|| panic!("test vector {} missing hash", $fname))
                .to_string();
            kat_canonical_json::<$ty>(&json, &expected);
        }
    };
}

macro_rules! kat_binary_test {
    ($name:ident, $ty:ty, $fname:expr, $hash_field:expr) => {
        #[test]
        fn $name() {
            let dir = vectors_dir();
            let path = std::path::Path::new(&dir).join($fname);
            let json = load_json(path.to_str().unwrap());
            let expected = json[$hash_field]
                .as_str()
                .unwrap_or_else(|| panic!("test vector {} missing hash", $fname))
                .to_string();
            kat_binary_signable::<$ty>(&json, &expected);
        }
    };
}

// Canonical JSON KATs
kat_test!(
    kat_json_node_admin_status,
    NodeAdminStatusV1,
    "node-admin-status-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_vault_admin_status,
    VaultAdminStatusV1,
    "vault-admin-status-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_admin_error_envelope,
    AdminErrorEnvelopeV1,
    "admin-error-envelope-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_audit_reference,
    AuditReferenceV1,
    "audit-reference-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_ledger_account,
    LedgerAccountV1,
    "ledger-account-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_ledger_journal,
    LedgerJournalV1,
    "ledger-journal-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_admin_p2p,
    AdminP2PV1,
    "admin-p2p-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_admin_onramp,
    AdminOnrampV1,
    "admin-onramp-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_admin_reconciliation,
    AdminReconciliationV1,
    "admin-reconciliation-v1.json",
    "expected_json_hash"
);
kat_test!(
    kat_json_admin_provider,
    AdminProviderV1,
    "admin-provider-v1.json",
    "expected_json_hash"
);

// Binary signing-bytes KATs
kat_binary_test!(
    kat_binary_node_admin_status,
    NodeAdminStatusV1,
    "node-admin-status-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_vault_admin_status,
    VaultAdminStatusV1,
    "vault-admin-status-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_admin_error_envelope,
    AdminErrorEnvelopeV1,
    "admin-error-envelope-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_audit_reference,
    AuditReferenceV1,
    "audit-reference-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_ledger_account,
    LedgerAccountV1,
    "ledger-account-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_ledger_journal,
    LedgerJournalV1,
    "ledger-journal-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_admin_p2p,
    AdminP2PV1,
    "admin-p2p-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_admin_onramp,
    AdminOnrampV1,
    "admin-onramp-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_admin_reconciliation,
    AdminReconciliationV1,
    "admin-reconciliation-v1.json",
    "expected_binary_hash"
);
kat_binary_test!(
    kat_binary_admin_provider,
    AdminProviderV1,
    "admin-provider-v1.json",
    "expected_binary_hash"
);
