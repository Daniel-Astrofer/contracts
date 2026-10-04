//! Discovery and membership wire types shared by peer and admission services.

use crate::canonical::{domain, field, integer, CanonicalSignable};
use serde::{de, Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::fmt;

/// Current version string for discovery and membership wire types.
pub const DISCOVERY_CONTRACT_VERSION: &str = "0.2.0";
/// Domain-separation bytes prepended when signing peer hello messages.
pub const PEER_HELLO_DOMAIN: &[u8] = b"KEROSENE_PEER_HELLO_V1";
/// Domain-separation bytes for candidate admission signatures.
pub const ADMISSION_REQUEST_DOMAIN: &[u8] = b"KEROSENE_ADMISSION_REQUEST_V1";
/// Domain-separation bytes for membership-manifest signatures.
pub const MEMBERSHIP_MANIFEST_DOMAIN: &[u8] = b"KEROSENE_MEMBERSHIP_MANIFEST_V1";
/// Domain-separation bytes for genesis trust-bundle signatures.
pub const GENESIS_TRUST_BUNDLE_DOMAIN: &[u8] = b"KEROSENE_GENESIS_TRUST_BUNDLE_V1";

/// Membership plane whose nodes share one trust and discovery configuration.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum DiscoveryPlane {
    /// Bank/KFE control-plane membership and peer network.
    Bank,
    /// Vault signing-plane membership and peer network.
    Vault,
}

impl DiscoveryPlane {
    /// Returns the stable lowercase value used in wire messages and signing.
    ///
    /// # Returns
    /// `bank` for the bank plane or `vault` for the vault plane.
    pub const fn as_str(self) -> &'static str {
        match self {
            Self::Bank => "bank",
            Self::Vault => "vault",
        }
    }
}

/// Member identity and root verification key included in the genesis trust bundle.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct TrustMember {
    /// Stable network member identifier.
    pub member_id: String,
    /// Ed25519 root public key encoded as 64 lowercase hexadecimal characters.
    pub root_public_key: String,
}

/// Genesis trust configuration for one membership plane and its voting threshold.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct TrustPlane {
    /// Minimum member count required for a valid quorum.
    pub threshold: u16,
    /// Configured members participating in this trust plane or manifest.
    pub members: Vec<TrustMember>,
}

/// Bootstrap trust roots for bank and vault planes on a network.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct GenesisTrustBundleV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Identifier of the distributed network this record belongs to.
    pub network_id: String,
    /// Genesis trust settings for the bank membership plane.
    pub bank: TrustPlane,
    /// Genesis trust settings for the vault membership plane.
    pub vault: TrustPlane,
    /// Bundle creation time as Unix epoch milliseconds.
    pub created_at_epoch_ms: u64,
}

/// Signed peer introduction that binds a member identity to a challenge and endpoint.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct PeerHelloV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Identifier of the distributed network this record belongs to.
    pub network_id: String,
    /// Membership or operational plane associated with this record.
    pub plane: DiscoveryPlane,
    /// Stable network member identifier.
    pub member_id: String,
    /// Ed25519 root public key encoded as lowercase hexadecimal.
    pub root_public_key: String,
    /// One-time cryptographic challenge the peer must sign.
    pub challenge: String,
    /// Challenge issue time as Unix epoch milliseconds.
    pub issued_at_epoch_ms: u64,
    /// Network endpoint advertised for peer communication.
    pub endpoint: String,
    /// Ed25519 signature encoded as 128 lowercase hexadecimal characters.
    pub signature: String,
}

/// Candidate request sponsored by a member and bound to a discovery challenge.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdmissionRequestV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Identifier of the distributed network this record belongs to.
    pub network_id: String,
    /// Membership or operational plane associated with this record.
    pub plane: DiscoveryPlane,
    /// Member identity proposed for admission.
    pub candidate: ManifestMember,
    /// Existing member sponsoring the candidate admission.
    pub sponsor_id: String,
    /// One-time cryptographic challenge the peer must sign.
    pub challenge: String,
    /// Challenge issue time as Unix epoch milliseconds.
    pub issued_at_epoch_ms: u64,
    /// Candidate Ed25519 signature encoded as 128 lowercase hexadecimal characters.
    pub signature: String,
}

/// Membership transition phase represented by a manifest.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum MembershipPhase {
    /// All members belong to one settled membership epoch.
    Stable,
    /// Transition phase requiring old and new membership sets to overlap under quorum rules.
    Joint,
}

/// Member identity, root key, and reachable endpoint in a membership manifest.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct ManifestMember {
    /// Stable network member identifier.
    pub member_id: String,
    /// Ed25519 root public key encoded as lowercase hexadecimal.
    pub root_public_key: String,
    /// Network endpoint advertised for peer communication.
    pub endpoint: String,
}

/// Member signature over the canonical membership-manifest bytes.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct ManifestSignature {
    /// Member identifier whose key produced this signature.
    pub signer_id: String,
    /// Ed25519 signature over the contract’s canonical signing bytes.
    pub signature: String,
}

/// Signed membership state and epoch used to verify network membership.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct MembershipManifestV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Identifier of the distributed network this record belongs to.
    pub network_id: String,
    /// Membership or operational plane associated with this record.
    pub plane: DiscoveryPlane,
    /// Monotonic membership-manifest revision.
    pub epoch: u64,
    /// Stable or joint-consensus phase represented by the manifest.
    pub phase: MembershipPhase,
    /// Digest of the prior manifest, empty only for the initial state.
    pub previous_manifest_hash: String,
    /// Minimum number of member votes/signers required by the configured policy.
    pub threshold: u16,
    /// Configured members participating in this trust plane or manifest.
    pub members: Vec<ManifestMember>,
    /// Required only during joint consensus and names the intended stable epoch.
    /// A value of `Some(0)` is invalid (schema minimum is 1) and will fail
    /// deserialization with a clear error message.
    #[serde(default, deserialize_with = "deserialize_next_epoch")]
    pub next_epoch: Option<u64>,
    /// Member signatures that authenticate this manifest version.
    pub signatures: Vec<ManifestSignature>,
}

impl CanonicalSignable for GenesisTrustBundleV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(GENESIS_TRUST_BUNDLE_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.network_id.as_bytes());
        trust_plane(&mut out, &self.bank);
        trust_plane(&mut out, &self.vault);
        integer(&mut out, self.created_at_epoch_ms);
        out
    }
}

impl CanonicalSignable for PeerHelloV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(PEER_HELLO_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.network_id.as_bytes());
        field(&mut out, self.plane.as_str().as_bytes());
        field(&mut out, self.member_id.as_bytes());
        field(&mut out, self.root_public_key.as_bytes());
        field(&mut out, self.challenge.as_bytes());
        integer(&mut out, self.issued_at_epoch_ms);
        field(&mut out, self.endpoint.as_bytes());
        out
    }
}

impl CanonicalSignable for AdmissionRequestV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMISSION_REQUEST_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.network_id.as_bytes());
        field(&mut out, self.plane.as_str().as_bytes());
        field(&mut out, self.candidate.member_id.as_bytes());
        field(&mut out, self.candidate.root_public_key.as_bytes());
        field(&mut out, self.candidate.endpoint.as_bytes());
        field(&mut out, self.sponsor_id.as_bytes());
        field(&mut out, self.challenge.as_bytes());
        integer(&mut out, self.issued_at_epoch_ms);
        out
    }
}

impl CanonicalSignable for MembershipManifestV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(MEMBERSHIP_MANIFEST_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.network_id.as_bytes());
        field(&mut out, self.plane.as_str().as_bytes());
        integer(&mut out, self.epoch);
        field(
            &mut out,
            match self.phase {
                MembershipPhase::Stable => b"stable",
                MembershipPhase::Joint => b"joint",
            },
        );
        field(&mut out, self.previous_manifest_hash.as_bytes());
        integer(&mut out, u64::from(self.threshold));
        integer(&mut out, self.members.len() as u64);
        for member in &self.members {
            field(&mut out, member.member_id.as_bytes());
            field(&mut out, member.root_public_key.as_bytes());
            field(&mut out, member.endpoint.as_bytes());
        }
        match self.next_epoch {
            Some(epoch) => {
                field(&mut out, b"some");
                integer(&mut out, epoch);
            }
            None => field(&mut out, b"none"),
        }
        out
    }
}

/// Derives a stable member identifier from the network ID and Ed25519 root public key.
///
/// # Parameters
/// * `network_id` - Network namespace preventing cross-network identity reuse.
/// * `root_public_key` - Raw 32-byte Ed25519 root public key.
///
/// # Returns
/// Lowercase hexadecimal SHA-256 digest of the concatenated inputs.
pub fn member_id(network_id: &str, root_public_key: &[u8; 32]) -> String {
    let mut digest = Sha256::new();
    digest.update(network_id.as_bytes());
    digest.update(root_public_key);
    hex::encode(digest.finalize())
}

fn trust_plane(out: &mut Vec<u8>, plane: &TrustPlane) {
    integer(out, u64::from(plane.threshold));
    integer(out, plane.members.len() as u64);
    for member in &plane.members {
        field(out, member.member_id.as_bytes());
        field(out, member.root_public_key.as_bytes());
    }
}

/// Custom deserializer for `Option<u64>` that rejects `Some(0)`.
///
/// The JSON schema requires `next_epoch >= 1` when present. This enforces
/// that constraint at deserialization time to prevent invalid state from
/// entering the system through serde-parse boundaries.
pub(crate) fn deserialize_next_epoch<'de, D>(deserializer: D) -> Result<Option<u64>, D::Error>
where
    D: de::Deserializer<'de>,
{
    struct NextEpochVisitor;
    impl<'de> de::Visitor<'de> for NextEpochVisitor {
        type Value = Option<u64>;

        fn expecting(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
            f.write_str("an integer >= 1, null, or absent field")
        }

        fn visit_none<E: de::Error>(self) -> Result<Option<u64>, E> {
            Ok(None)
        }

        fn visit_some<D: de::Deserializer<'de>>(
            self,
            deserializer: D,
        ) -> Result<Option<u64>, D::Error> {
            u64::deserialize(deserializer).and_then(|v| {
                if v == 0 {
                    Err(de::Error::custom("next_epoch must be >= 1 when present"))
                } else {
                    Ok(Some(v))
                }
            })
        }

        fn visit_unit<E: de::Error>(self) -> Result<Option<u64>, E> {
            Ok(None)
        }

        fn visit_u64<E: de::Error>(self, value: u64) -> Result<Option<u64>, E> {
            if value == 0 {
                Err(de::Error::custom("next_epoch must be >= 1 when present"))
            } else {
                Ok(Some(value))
            }
        }
    }
    deserializer.deserialize_option(NextEpochVisitor)
}
