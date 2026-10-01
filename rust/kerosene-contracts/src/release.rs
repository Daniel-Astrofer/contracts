//! Release observer wire contracts. This typed source generates the JSON schemas.
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use serde_json::Value;

/// Decode protocol JSON without silently collapsing duplicate object fields,
/// including untyped release-lock subtrees. Typed field validation follows.
pub fn decode_release_json<T: serde::de::DeserializeOwned>(
    bytes: &[u8],
) -> Result<T, serde_json::Error> {
    struct Unique(Value);
    impl<'de> Deserialize<'de> for Unique {
        fn deserialize<D: serde::Deserializer<'de>>(deserializer: D) -> Result<Self, D::Error> {
            struct Visitor;
            impl<'de> serde::de::Visitor<'de> for Visitor {
                type Value = Unique;
                fn expecting(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
                    f.write_str("JSON with unique object keys")
                }
                fn visit_bool<E: serde::de::Error>(self, v: bool) -> Result<Unique, E> {
                    Ok(Unique(Value::Bool(v)))
                }
                fn visit_i64<E: serde::de::Error>(self, v: i64) -> Result<Unique, E> {
                    Ok(Unique(Value::from(v)))
                }
                fn visit_u64<E: serde::de::Error>(self, v: u64) -> Result<Unique, E> {
                    Ok(Unique(Value::from(v)))
                }
                fn visit_f64<E: serde::de::Error>(self, v: f64) -> Result<Unique, E> {
                    Ok(Unique(Value::from(v)))
                }
                fn visit_str<E: serde::de::Error>(self, v: &str) -> Result<Unique, E> {
                    Ok(Unique(Value::String(v.into())))
                }
                fn visit_string<E: serde::de::Error>(self, v: String) -> Result<Unique, E> {
                    Ok(Unique(Value::String(v)))
                }
                fn visit_unit<E: serde::de::Error>(self) -> Result<Unique, E> {
                    Ok(Unique(Value::Null))
                }
                fn visit_seq<A: serde::de::SeqAccess<'de>>(
                    self,
                    mut seq: A,
                ) -> Result<Unique, A::Error> {
                    let mut values = Vec::new();
                    while let Some(v) = seq.next_element::<Unique>()? {
                        values.push(v.0);
                    }
                    Ok(Unique(Value::Array(values)))
                }
                fn visit_map<A: serde::de::MapAccess<'de>>(
                    self,
                    mut map: A,
                ) -> Result<Unique, A::Error> {
                    let mut values = serde_json::Map::new();
                    while let Some((key, v)) = map.next_entry::<String, Unique>()? {
                        if values.insert(key, v.0).is_some() {
                            return Err(serde::de::Error::custom("duplicate JSON object field"));
                        }
                    }
                    Ok(Unique(Value::Object(values)))
                }
            }
            deserializer.deserialize_any(Visitor)
        }
    }
    let unique: Unique = serde_json::from_slice(bytes)?;
    serde_json::from_value(unique.0)
}

pub const RELEASE_OBSERVER_VERSION: &str = "1.0.0";
pub const REPORT_SCHEMA: &str = "kerosene.bank-observer-report/v2";
pub const BANK_READ_SCHEMA: &str = "kerosene.bank-release-read/v1";
pub const OBSERVATION_SCHEMA: &str = "kerosene.signed-release-observation/v1";
pub const READS_SCHEMA: &str = "kerosene.release-observations/v1";
pub const MAX_OBSERVERS: usize = 64;
pub const MAX_SEQUENCE: u64 = 9_007_199_254_740_991;
pub const MAX_REPORT_LIFETIME_SECS: i64 = 3600;
pub const MAX_OBSERVATION_AGE_SECS: i64 = 900;
pub const MAX_CLOCK_SKEW_SECS: i64 = 300;
pub const MAX_BANK_READ_LIFETIME_SECS: i64 = 60;
pub const MAX_BODY_BYTES: usize = 262_144;
pub const IDENTIFIER_PATTERN: &str = "^[a-z0-9][a-z0-9._-]{2,127}$";
pub const DIGEST_PATTERN: &str = "^sha256:[0-9a-f]{64}$";

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct ObserverDiscoveryV1 {
    pub schema: String,
    pub observer_id: String,
    pub network_id: String,
    pub public_key_der_base64: String,
    pub bank_observers: Vec<String>,
    pub report_schema: String,
    pub max_report_lifetime_seconds: i64,
    pub commit_certificate: CommitCertificateAvailability,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "lowercase")]
pub enum Compatibility {
    Compatible,
    Incompatible,
    Unknown,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct ReleaseSignatureV1 {
    pub member_id: String,
    pub public_key_der_base64: String,
    pub signature_base64: String,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct BankObservationV2 {
    pub observer_id: String,
    pub status: Compatibility,
    pub observed_sequence: u64,
    pub release_digest: String,
    pub observed_at: String,
}

// A single field declaration keeps signed and unsigned consumers synchronized.
macro_rules! signed_contract {
    ($payload:ident, $signed:ident { $($field:ident: $ty:ty),* $(,)? }) => {
        #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
        #[serde(rename_all = "camelCase", deny_unknown_fields)]
        pub struct $payload { $(pub $field: $ty,)* }
        #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
        #[serde(rename_all = "camelCase", deny_unknown_fields)]
        pub struct $signed {
            $(pub $field: $ty,)*
            pub signatures: Vec<ReleaseSignatureV1>,
        }
        impl $payload {
            pub fn signed(self, signatures: Vec<ReleaseSignatureV1>) -> $signed {
                $signed { $($field: self.$field,)* signatures }
            }
        }
        impl $signed {
            pub fn payload(&self) -> $payload {
                $payload { $($field: self.$field.clone(),)* }
            }
        }
    }
}

signed_contract!(BankObserverReportPayloadV2, BankObserverReportV2 {
    schema: String,
    release_id: String,
    network_id: String,
    target_sequence: u64,
    release_lock_canonical_digest: String,
    issued_at: String,
    expires_at: String,
    observations: Vec<BankObservationV2>,
});

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "kebab-case")]
pub enum BankReadSource {
    BankRuntime,
    Synthetic,
}

signed_contract!(
    BankReleaseReadPayloadV1,
    BankReleaseReadV1 {
        schema: String,
        release_id: String,
        network_id: String,
        target_sequence: u64,
        release_lock_canonical_digest: String,
        issued_at: String,
        expires_at: String,
        challenge: String,
        source: BankReadSource,
        observation: BankObservationV2,
    }
);

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "lowercase")]
pub enum CommitCertificateAvailability {
    Unavailable,
}

signed_contract!(
    SignedReleaseObservationPayloadV1,
    SignedReleaseObservationV1 {
        schema: String,
        release_id: String,
        network_id: String,
        target_sequence: u64,
        release_lock_canonical_digest: String,
        issued_at: String,
        expires_at: String,
        observation: BankObservationV2,
        bank_read: BankReleaseReadV1,
        commit_certificate: CommitCertificateAvailability,
    }
);

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct ReleaseObservationsV1 {
    pub schema: String,
    pub observations: Vec<SignedReleaseObservationV1>,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct ReleaseObserverRequestV1 {
    pub release_lock: Value,
    pub report: BankObserverReportPayloadV2,
}

pub fn valid_identifier(value: &str) -> bool {
    (3..=128).contains(&value.len())
        && (value.as_bytes()[0].is_ascii_lowercase() || value.as_bytes()[0].is_ascii_digit())
        && value
            .bytes()
            .all(|c| c.is_ascii_lowercase() || c.is_ascii_digit() || b"._-".contains(&c))
}

pub fn valid_digest(value: &str) -> bool {
    value.len() == 71
        && value.starts_with("sha256:")
        && value[7..]
            .bytes()
            .all(|c| c.is_ascii_digit() || (b'a'..=b'f').contains(&c))
}

macro_rules! wire {
    ($name:ident { $($field:ident: $ty:ty),* $(,)? }) => {
        #[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
        #[serde(rename_all = "camelCase", deny_unknown_fields)]
        pub struct $name { $(pub $field: $ty,)* }
    }
}

wire!(ReleaseApprovalV1 {
    schema: String,
    network_id: String,
    epoch: u64,
    sequence: u64,
    release_lock_canonical_digest: String,
    previous_approval_digest: String,
});
wire!(BftAuthorizationV3 {
    network_id: String,
    epoch: u64,
    threshold: u64,
    members: u64
});
wire!(ReleaseNetworkV3 {
    id: String,
    plane: String
});
wire!(TufAuthorizationV3 {
    target_path: String
});
wire!(VaultCompatibilityV3 {
    threshold: u64,
    members: u64,
    attestation_digest: String
});
wire!(ReleaseAuthorizationV3 {
    tuf: TufAuthorizationV3,
    bft: BftAuthorizationV3,
    vault_compatibility: VaultCompatibilityV3
});
wire!(SourceBundleV3 {
    uri: String,
    digest: String
});
wire!(SourceRepositoryV3 {
    commit: String,
    bundle_digest: String
});
wire!(SourceRepositoriesV3 {
    admin: SourceRepositoryV3,
    clients: SourceRepositoryV3,
    contracts: SourceRepositoryV3,
    core: SourceRepositoryV3,
    deploy: SourceRepositoryV3,
    kfe: SourceRepositoryV3,
    node: SourceRepositoryV3,
    rails: SourceRepositoryV3,
    shared: SourceRepositoryV3,
    vault: SourceRepositoryV3,
});
wire!(ReleaseSourceV3 {
    bundle: SourceBundleV3,
    repositories: SourceRepositoriesV3
});
wire!(ReleaseServiceV3 {
    image: String,
    config_digest: String,
    repository: String,
    rollout: String
});
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase", deny_unknown_fields)]
pub struct ReleaseServicesV3 {
    pub admin: ReleaseServiceV3,
    pub core: ReleaseServiceV3,
    pub kfe: ReleaseServiceV3,
    pub node: ReleaseServiceV3,
    pub vault: ReleaseServiceV3,
    #[serde(rename = "web-page")]
    pub web_page: ReleaseServiceV3,
    pub postgres: ReleaseServiceV3,
    pub redis: ReleaseServiceV3,
    pub bitcoin: ReleaseServiceV3,
    pub lnd: ReleaseServiceV3,
    pub tor: ReleaseServiceV3,
}
wire!(ReleasePolicyV3 {
    allow_source_build: bool,
    vault_signer_activation: bool,
    minimum_bank_observers: u64
});
wire!(ReleaseMigrationV3 {
    id: String,
    classification: String,
    snapshot_required: bool,
    recovery_evidence_digest: String
});
wire!(ReleaseLockV3 {
    schema: String,
    schema_version: u8,
    release_id: String,
    sequence: u64,
    network: ReleaseNetworkV3,
    authorization: ReleaseAuthorizationV3,
    source: ReleaseSourceV3,
    services: ReleaseServicesV3,
    policy: ReleasePolicyV3,
    migration: ReleaseMigrationV3,
});

impl ReleaseApprovalV1 {
    /// Stateless validation. Consensus owns epoch/sequence/previous-digest state.
    pub fn validate(&self) -> Result<(), &'static str> {
        if self.schema != "kerosene.release-approval/v1"
            || !valid_identifier(&self.network_id)
            || self.epoch == 0
            || self.epoch > MAX_SEQUENCE
            || self.sequence == 0
            || self.sequence > MAX_SEQUENCE
            || !valid_digest(&self.release_lock_canonical_digest)
            || !valid_digest(&self.previous_approval_digest)
        {
            return Err("invalid release approval");
        }
        Ok(())
    }
    pub fn digest(&self) -> String {
        format!("sha256:{}", crate::canonical_json_hash(self))
    }
}

impl ReleaseLockV3 {
    pub fn validate_governance(&self) -> Result<(), &'static str> {
        let bft = &self.authorization.bft;
        if self.schema != "kerosene.release-lock/v3"
            || self.schema_version != 3
            || !valid_identifier(&self.release_id)
            || !valid_identifier(&self.network.id)
            || self.network.plane != "bank"
            || !valid_identifier(&bft.network_id)
            || self.sequence == 0
            || self.sequence > MAX_SEQUENCE
            || bft.epoch == 0
            || bft.epoch > MAX_SEQUENCE
            || !(4..=MAX_OBSERVERS as u64).contains(&bft.members)
            || bft.threshold <= 2 * bft.members / 3
            || bft.threshold > bft.members
            || self.policy.minimum_bank_observers < bft.threshold
            || self.policy.minimum_bank_observers > bft.members
            || self.policy.allow_source_build
            || self.policy.vault_signer_activation
            || self.authorization.tuf.target_path != format!("releases/{}.json", self.release_id)
        {
            return Err("invalid v3 release governance bounds");
        }
        Ok(())
    }
}

/// Python deploy canonicalization and Rust agree on this bounded JSON subset.
/// Floats and integers outside the interoperable exact range are rejected.
pub fn canonical_release_digest(value: &Value) -> Result<String, &'static str> {
    fn check(value: &Value) -> bool {
        match value {
            Value::Number(n) => {
                n.as_i64().is_some_and(|v| v.unsigned_abs() <= MAX_SEQUENCE)
                    || n.as_u64().is_some_and(|v| v <= MAX_SEQUENCE)
            }
            Value::Array(a) => a.iter().all(check),
            Value::Object(o) => o.values().all(check),
            _ => true,
        }
    }
    if !value.is_object() || !check(value) {
        return Err("release lock must be an object using exact-range integers only");
    }
    Ok(format!("sha256:{}", crate::canonical_json_hash(value)))
}
