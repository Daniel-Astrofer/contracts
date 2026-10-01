use base64::{engine::general_purpose::STANDARD, Engine};
use ed25519_dalek::{pkcs8::DecodePublicKey, Signature, VerifyingKey};
use kerosene_contracts::{canonical_json_bytes, canonical_json_hash, release::*};
use serde_json::{json, Value};

fn fixture() -> Value {
    serde_json::from_str(include_str!(
        "../../../test-vectors/release-observer-v1.json"
    ))
    .unwrap()
}
fn lock() -> Value {
    serde_json::from_str(include_str!("../../../test-vectors/release-lock-v3.json")).unwrap()
}
fn verify<T: serde::Serialize>(payload: &T, signature: &ReleaseSignatureV1) {
    let key = VerifyingKey::from_public_key_der(
        &STANDARD.decode(&signature.public_key_der_base64).unwrap(),
    )
    .unwrap();
    let sig =
        Signature::from_slice(&STANDARD.decode(&signature.signature_base64).unwrap()).unwrap();
    key.verify_strict(&canonical_json_bytes(payload), &sig)
        .unwrap();
}
#[test]
fn fixed_hashes_signatures_and_unsigned_payloads_roundtrip() {
    let vector = fixture();
    assert_eq!(
        canonical_release_digest(&lock()).unwrap(),
        vector["releaseLockCanonicalDigest"]
    );
    let report: BankObserverReportV2 = serde_json::from_value(vector["report"].clone()).unwrap();
    let bank: BankReleaseReadV1 = serde_json::from_value(vector["bankRead"].clone()).unwrap();
    let reads: ReleaseObservationsV1 = serde_json::from_value(vector["reads"].clone()).unwrap();
    let approval: ReleaseApprovalV1 = serde_json::from_value(vector["approval"].clone()).unwrap();
    approval.validate().unwrap();
    let pairs = [
        (
            serde_json::to_value(report.payload()).unwrap(),
            "reportPayloadCanonicalDigest",
        ),
        (
            serde_json::to_value(bank.payload()).unwrap(),
            "bankReadPayloadCanonicalDigest",
        ),
        (
            serde_json::to_value(reads.observations[0].payload()).unwrap(),
            "observationPayloadCanonicalDigest",
        ),
    ];
    for (payload, field) in pairs {
        assert_eq!(
            format!("sha256:{}", canonical_json_hash(&payload)),
            vector[field]
        );
        assert!(payload.get("signatures").is_none());
    }
    verify(&report.payload(), &report.signatures[0]);
    verify(&bank.payload(), &bank.signatures[0]);
    verify(
        &reads.observations[0].payload(),
        &reads.observations[0].signatures[0],
    );
    assert_eq!(approval.digest(), vector["approvalCanonicalDigest"]);
}
#[test]
fn generated_schemas_validate_fixtures_and_reject_wrong_tags_and_circular_v3_fields() {
    let vector = fixture();
    let schemas = [
        (
            include_str!("../../../schemas/release/bank-observer-report-v2.schema.json"),
            vector["report"].clone(),
        ),
        (
            include_str!("../../../schemas/release/bank-release-read-v1.schema.json"),
            vector["bankRead"].clone(),
        ),
        (
            include_str!("../../../schemas/release/release-observations-v1.schema.json"),
            vector["reads"].clone(),
        ),
        (
            include_str!("../../../schemas/release/release-lock-v3.schema.json"),
            lock(),
        ),
        (
            include_str!("../../../schemas/release/release-approval-v1.schema.json"),
            vector["approval"].clone(),
        ),
    ];
    for (schema, input) in schemas {
        let schema: Value = serde_json::from_str(schema).unwrap();
        let validator = jsonschema::validator_for(&schema).unwrap();
        validator.validate(&input).unwrap();
        let mut wrong = input.clone();
        wrong["schema"] = json!("wrong/v999");
        assert!(!validator.is_valid(&wrong));
        let mut extra = input;
        extra["callerCompatible"] = json!(true);
        assert!(!validator.is_valid(&extra));
    }
    let schema: Value = serde_json::from_str(include_str!(
        "../../../schemas/release/release-lock-v3.schema.json"
    ))
    .unwrap();
    let validator = jsonschema::validator_for(&schema).unwrap();
    for field in ["height", "commitDigest"] {
        let mut wrong = lock();
        wrong["authorization"]["bft"][field] = json!(1);
        assert!(!validator.is_valid(&wrong));
        assert!(serde_json::from_value::<ReleaseLockV3>(wrong).is_err());
    }
}
#[test]
fn v3_governance_and_approval_validation_are_bounded_and_separate() {
    let mut typed: ReleaseLockV3 = serde_json::from_value(lock()).unwrap();
    typed.validate_governance().unwrap();
    assert_ne!(typed.network.id, typed.authorization.bft.network_id);
    typed.authorization.bft.threshold = 2;
    assert!(typed.validate_governance().is_err());
    let mut approval: ReleaseApprovalV1 =
        serde_json::from_value(fixture()["approval"].clone()).unwrap();
    approval.epoch = 0;
    assert!(approval.validate().is_err());
    let mut extra = fixture()["approval"].clone();
    extra["blockHash"] = json!("not a tx field");
    assert!(serde_json::from_value::<ReleaseApprovalV1>(extra).is_err());
    assert!(canonical_release_digest(&json!({"n":9007199254740992u64})).is_err());
    assert!(canonical_release_digest(&json!({"n":1.0})).is_err());
    assert!(!valid_identifier("Bad"));
    assert!(!valid_digest("sha256:abc"));
}
#[test]
fn report_v2_stays_exactly_deploy_compatible() {
    let vector = fixture();
    let report: BankObserverReportV2 = serde_json::from_value(vector["report"].clone()).unwrap();
    let mut raw = vector["report"].clone();
    raw.as_object_mut().unwrap().remove("signatures");
    assert_eq!(
        canonical_json_bytes(&raw),
        canonical_json_bytes(&report.payload())
    );
    assert!(
        serde_json::from_value::<BankObserverReportPayloadV2>(vector["report"].clone()).is_err()
    );
    let mut tampered = report.payload();
    tampered.target_sequence += 1;
    let signature = &report.signatures[0];
    let key = VerifyingKey::from_public_key_der(
        &STANDARD.decode(&signature.public_key_der_base64).unwrap(),
    )
    .unwrap();
    let sig =
        Signature::from_slice(&STANDARD.decode(&signature.signature_base64).unwrap()).unwrap();
    assert!(key
        .verify_strict(&canonical_json_bytes(&tampered), &sig)
        .is_err());
}

#[test]
fn duplicate_fields_in_untyped_release_locks_are_rejected_recursively() {
    let bytes = br#"{"releaseLock":{"network":{"id":"one","id":"two"}}}"#;
    assert!(decode_release_json::<Value>(bytes).is_err());
    assert!(decode_release_json::<Value>(br#"{"values":[{"sequence":1,"sequence":2}]}"#).is_err());
    assert_eq!(
        decode_release_json::<Value>(br#"{"values":[1,null,true,"hello"]}"#).unwrap(),
        json!({"values":[1,null,true,"hello"]})
    );
}
