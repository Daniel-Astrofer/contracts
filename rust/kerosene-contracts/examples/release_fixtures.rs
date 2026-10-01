//! Deterministic public fixtures, not operational Bank evidence or credentials.
use base64::{engine::general_purpose::STANDARD, Engine};
use ed25519_dalek::{pkcs8::EncodePublicKey, Signer, SigningKey};
use kerosene_contracts::{canonical_json_bytes, canonical_json_hash, release::*};
use serde::Serialize;

fn sign<T: Serialize>(key: &SigningKey, id: &str, payload: &T) -> ReleaseSignatureV1 {
    ReleaseSignatureV1 {
        member_id: id.into(),
        public_key_der_base64: STANDARD
            .encode(key.verifying_key().to_public_key_der().unwrap().as_bytes()),
        signature_base64: STANDARD.encode(key.sign(&canonical_json_bytes(payload)).to_bytes()),
    }
}
fn main() {
    let lock: serde_json::Value =
        serde_json::from_str(include_str!("../../../test-vectors/release-lock-v3.json")).unwrap();
    let digest = canonical_release_digest(&lock).unwrap();
    let node = SigningKey::from_bytes(&[7; 32]);
    let bank = SigningKey::from_bytes(&[8; 32]);
    let observation = BankObservationV2 {
        observer_id: "bank-001".into(),
        status: Compatibility::Compatible,
        observed_sequence: 42,
        release_digest: digest.clone(),
        observed_at: "2026-10-01T21:00:00Z".into(),
    };
    let report_payload = BankObserverReportPayloadV2 {
        schema: REPORT_SCHEMA.into(),
        release_id: lock["releaseId"].as_str().unwrap().into(),
        network_id: lock["network"]["id"].as_str().unwrap().into(),
        target_sequence: 42,
        release_lock_canonical_digest: digest.clone(),
        issued_at: "2026-10-01T21:00:00Z".into(),
        expires_at: "2026-10-01T21:05:00Z".into(),
        observations: ["bank-001", "bank-002", "bank-003"]
            .into_iter()
            .map(|id| {
                let mut entry = observation.clone();
                entry.observer_id = id.into();
                entry
            })
            .collect(),
    };
    let bank_payload = BankReleaseReadPayloadV1 {
        schema: BANK_READ_SCHEMA.into(),
        release_id: report_payload.release_id.clone(),
        network_id: report_payload.network_id.clone(),
        target_sequence: 42,
        release_lock_canonical_digest: digest.clone(),
        issued_at: report_payload.issued_at.clone(),
        expires_at: "2026-10-01T21:00:45Z".into(),
        challenge: "a".repeat(64),
        source: BankReadSource::BankRuntime,
        observation: observation.clone(),
    };
    let bank_signature = sign(&bank, "bank-001", &bank_payload);
    let read = bank_payload.clone().signed(vec![bank_signature]);
    let wrapper_payload = SignedReleaseObservationPayloadV1 {
        schema: OBSERVATION_SCHEMA.into(),
        release_id: report_payload.release_id.clone(),
        network_id: report_payload.network_id.clone(),
        target_sequence: 42,
        release_lock_canonical_digest: digest.clone(),
        issued_at: report_payload.issued_at.clone(),
        expires_at: read.expires_at.clone(),
        observation,
        bank_read: read.clone(),
        commit_certificate: CommitCertificateAvailability::Unavailable,
    };
    let signature = sign(&node, "bank-001", &wrapper_payload);
    let report_signatures = [("bank-001", 7u8), ("bank-002", 9), ("bank-003", 10)]
        .into_iter()
        .map(|(id, seed)| sign(&SigningKey::from_bytes(&[seed; 32]), id, &report_payload))
        .collect();
    let roster_members = [
        ("bank-001", 7u8),
        ("bank-002", 9),
        ("bank-003", 10),
        ("bank-004", 11),
    ]
    .into_iter()
    .map(|(id, seed)| {
        (
            id,
            STANDARD.encode(
                SigningKey::from_bytes(&[seed; 32])
                    .verifying_key()
                    .to_public_key_der()
                    .unwrap()
                    .as_bytes(),
            ),
        )
    })
    .collect::<std::collections::BTreeMap<_, _>>();
    let roster = serde_json::json!({"schema":"kerosene.release-roster/v1",
        "networkId":lock["authorization"]["bft"]["networkId"], "members":roster_members});
    let approval = ReleaseApprovalV1 {
        schema: "kerosene.release-approval/v1".into(),
        network_id: lock["authorization"]["bft"]["networkId"]
            .as_str()
            .unwrap()
            .into(),
        epoch: 1,
        sequence: 42,
        release_lock_canonical_digest: digest.clone(),
        previous_approval_digest: format!("sha256:{}", "a".repeat(64)),
    };
    let fixture = serde_json::json!({
        "fixtureOnly":true,"releaseLockCanonicalDigest":digest,
        "report":report_payload.clone().signed(report_signatures), "roster":roster,
        "bankRead":read,
        "reads":ReleaseObservationsV1 {schema:READS_SCHEMA.into(),observations:vec![wrapper_payload.clone().signed(vec![signature])]},
        "approval":approval,"approvalCanonicalDigest":approval.digest(),
        "reportPayloadCanonicalDigest":format!("sha256:{}",canonical_json_hash(&report_payload)),
        "bankReadPayloadCanonicalDigest":format!("sha256:{}",canonical_json_hash(&bank_payload)),
        "observationPayloadCanonicalDigest":format!("sha256:{}",canonical_json_hash(&wrapper_payload)),
    });
    println!("{}", serde_json::to_string_pretty(&fixture).unwrap());
}
