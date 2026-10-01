//! Print generated schemas; --check verifies committed artifacts without writes.
use kerosene_contracts::release::*;
use schemars::{schema_for, JsonSchema};

fn schema<T: JsonSchema>(name: &str, check: bool) {
    let mut value = serde_json::to_value(schema_for!(T)).unwrap();
    decorate(
        &mut value,
        std::any::type_name::<T>().rsplit("::").next().unwrap(),
    );
    value["$id"] = serde_json::json!(format!("urn:kerosene:{name}"));
    if check {
        let path = format!("schemas/release/{name}.schema.json");
        let actual: serde_json::Value =
            serde_json::from_slice(&std::fs::read(path).unwrap()).unwrap();
        assert!(actual == value, "generated schema drift: {name}");
    } else {
        println!("{name}\n{}", serde_json::to_string_pretty(&value).unwrap());
    }
}
fn main() {
    let check = std::env::args().any(|v| v == "--check");
    schema::<BankObserverReportV2>("bank-observer-report-v2", check);
    schema::<BankReleaseReadV1>("bank-release-read-v1", check);
    schema::<ReleaseObservationsV1>("release-observations-v1", check);
    schema::<ReleaseObserverRequestV1>("release-observer-request-v1", check);
    schema::<ObserverDiscoveryV1>("release-observer-discovery-v1", check);
    schema::<ReleaseLockV3>("release-lock-v3", check);
    schema::<ReleaseApprovalV1>("release-approval-v1", check);
}

// Wire constraints and schema tags augment the derived shapes. Constants and
// types remain in the single Rust contract source; there is no copied schema.
fn decorate(value: &mut serde_json::Value, name: &str) {
    use serde_json::json;
    if let Some(definitions) = value.get_mut("definitions").and_then(|v| v.as_object_mut()) {
        for (name, definition) in definitions {
            decorate(definition, name);
        }
    }
    let Some(properties) = value.get_mut("properties").and_then(|v| v.as_object_mut()) else {
        return;
    };
    for (field, schema) in properties.iter_mut() {
        let constraint = match field.as_str() {
            "observerId" | "memberId" | "networkId" | "releaseId" | "id" | "repository" => {
                Some(("pattern", json!(IDENTIFIER_PATTERN)))
            }
            "releaseLockCanonicalDigest"
            | "releaseDigest"
            | "previousApprovalDigest"
            | "digest"
            | "bundleDigest"
            | "configDigest"
            | "attestationDigest"
            | "recoveryEvidenceDigest" => Some(("pattern", json!(DIGEST_PATTERN))),
            "issuedAt" | "expiresAt" | "observedAt" => Some(("format", json!("date-time"))),
            "challenge" => Some(("pattern", json!("^[0-9a-f]{64}$"))),
            "commit" => Some(("pattern", json!("^(?:[0-9a-f]{40}|[0-9a-f]{64})$"))),
            "targetPath" => Some((
                "pattern",
                json!("^releases/[a-z0-9][a-z0-9._-]{2,127}\\.json$"),
            )),
            "uri" => Some(("pattern", json!("^oci://.+@sha256:[0-9a-f]{64}$"))),
            "image" => Some((
                "pattern",
                json!("^[A-Za-z0-9][A-Za-z0-9._/:@-]*@sha256:[0-9a-f]{64}$"),
            )),
            "rollout" => Some((
                "enum",
                json!([
                    "admin",
                    "application",
                    "network",
                    "operator",
                    "quorum",
                    "stateful"
                ]),
            )),
            "classification" => Some(("enum", json!(["reversible", "approved-recovery"]))),
            _ => None,
        };
        if let Some((key, constraint)) = constraint {
            schema[key] = constraint;
        }
        if ["targetSequence", "sequence", "epoch", "observedSequence"].contains(&field.as_str()) {
            schema["minimum"] = json!(if field == "observedSequence" { 0 } else { 1 });
            schema["maximum"] = json!(MAX_SEQUENCE);
        }
        if ["observations", "signatures", "bankObservers"].contains(&field.as_str()) {
            schema["minItems"] = json!(1);
            schema["maxItems"] = json!(MAX_OBSERVERS);
        }
        if ["publicKeyDerBase64", "signatureBase64"].contains(&field.as_str()) {
            schema["minLength"] = json!(1);
        }
    }
    let tag = match name {
        "BankObserverReportV2" | "BankObserverReportPayloadV2" => Some(REPORT_SCHEMA),
        "BankReleaseReadV1" | "BankReleaseReadPayloadV1" => Some(BANK_READ_SCHEMA),
        "SignedReleaseObservationV1" | "SignedReleaseObservationPayloadV1" => {
            Some(OBSERVATION_SCHEMA)
        }
        "ReleaseObservationsV1" => Some(READS_SCHEMA),
        "ObserverDiscoveryV1" => Some("kerosene.release-observer-discovery/v1"),
        "ReleaseLockV3" => Some("kerosene.release-lock/v3"),
        "ReleaseApprovalV1" => Some("kerosene.release-approval/v1"),
        _ => None,
    };
    if let Some(tag) = tag {
        properties.get_mut("schema").unwrap()["const"] = json!(tag);
    }
    match name {
        "ReleaseLockV3" => {
            properties.get_mut("schemaVersion").unwrap()["const"] = json!(3);
            properties.get_mut("schemaVersion").unwrap()["minimum"] = json!(0);
        }
        "ReleaseNetworkV3" => {
            properties.get_mut("plane").unwrap()["const"] = json!("bank");
        }
        "ReleasePolicyV3" => {
            properties.get_mut("allowSourceBuild").unwrap()["const"] = json!(false);
            properties.get_mut("vaultSignerActivation").unwrap()["const"] = json!(false);
            properties.get_mut("minimumBankObservers").unwrap()["minimum"] = json!(3);
        }
        "ReleaseMigrationV3" => {
            properties.get_mut("snapshotRequired").unwrap()["const"] = json!(true);
        }
        "BftAuthorizationV3" | "VaultCompatibilityV3" => {
            properties.get_mut("members").unwrap()["minimum"] =
                json!(if name == "BftAuthorizationV3" { 4 } else { 3 });
            properties.get_mut("members").unwrap()["maximum"] = json!(MAX_OBSERVERS);
            properties.get_mut("threshold").unwrap()["minimum"] =
                json!(if name == "BftAuthorizationV3" { 3 } else { 2 });
            properties.get_mut("threshold").unwrap()["maximum"] = json!(MAX_OBSERVERS);
        }
        _ => {}
    }
}
