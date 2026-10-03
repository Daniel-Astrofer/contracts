//! Synthetic interoperability vector only; never operational authorization.
use base64::{engine::general_purpose::STANDARD, Engine};
use ed25519_dalek::{Signer, SigningKey};
use kerosene_contracts::{canonical_json_bytes, release::*};
use serde_json::json;
use std::collections::BTreeMap;

fn main() {
    let admission = CellAdmissionV1 {
        schema: "kerosene.cell-admission/v1".into(),
        network_id: "bank-release-governance".into(),
        epoch: 1,
        cell_id: "cell-example".into(),
        cluster_uid: "80cf8d2f-172d-4d43-ba99-1734b32184b1".into(),
        release_approval_digest: format!("sha256:{}", "a".repeat(64)),
        operator_id: "operator-example".into(),
        change_id: "change-example".into(),
        issued_at_unix_seconds: 1000,
        expires_at_unix_seconds: 1100,
        nonce: "b".repeat(64),
    };
    let canonical = canonical_json_bytes(&admission);
    let mut members = BTreeMap::new();
    let mut signatures = Vec::new();
    // Publicly known synthetic seeds; distinct from all operational identities.
    for index in 1..=4u8 {
        let key = SigningKey::from_bytes(&[index + 40; 32]);
        let member = format!("validator-{index}");
        members.insert(
            member.clone(),
            STANDARD.encode(key.verifying_key().to_bytes()),
        );
        if index <= 3 {
            signatures.push(CellAdmissionSignatureV1 {
                member_id: member,
                signature_base64: STANDARD.encode(key.sign(&canonical).to_bytes()),
            });
        }
    }
    let digest = admission.digest();
    let envelope = CellAdmissionEnvelopeV1 {
        admission,
        signatures,
    };
    envelope.validate_structure_at(1000).unwrap();
    let vector = json!({"fixtureOnly":true, "policy":{"networkId":"bank-release-governance","epoch":1,"threshold":3,"members":members},
        "envelope":envelope,"canonicalPayloadUtf8":String::from_utf8(canonical).unwrap(),"admissionDigest":digest,"nowUnixSeconds":1000});
    let path = "test-vectors/cell-admission-v1.json";
    if std::env::args().any(|v| v == "--check") {
        let actual: serde_json::Value =
            serde_json::from_slice(&std::fs::read(path).unwrap()).unwrap();
        assert_eq!(actual, vector, "Cell admission fixture drift");
    } else if std::env::args().any(|v| v == "--write") {
        std::fs::write(
            path,
            format!("{}\n", serde_json::to_string_pretty(&vector).unwrap()),
        )
        .unwrap();
    } else {
        println!("{}", serde_json::to_string_pretty(&vector).unwrap());
    }
}
