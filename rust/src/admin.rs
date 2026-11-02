//! Administrative wire types for node, vault, ledger, and provider observation.

use crate::canonical::{domain, field, integer, sort_value, CanonicalSignable};
use crate::discovery::DiscoveryPlane;
use serde::{Deserialize, Serialize};

/// Current version string for administrative wire types.
pub const ADMIN_CONTRACT_VERSION: &str = "0.1.0";
/// Signature domain for node readiness status payloads.
pub const ADMIN_NODE_STATUS_DOMAIN: &[u8] = b"KEROSENE_ADMIN_NODE_STATUS_V1";
/// Signature domain for vault readiness status payloads.
pub const ADMIN_VAULT_STATUS_DOMAIN: &[u8] = b"KEROSENE_ADMIN_VAULT_STATUS_V1";
/// Signature domain for administrative error envelopes.
pub const ADMIN_ERROR_ENVELOPE_DOMAIN: &[u8] = b"KEROSENE_ADMIN_ERROR_ENVELOPE_V1";
/// Signature domain for audit-event references.
pub const ADMIN_AUDIT_REFERENCE_DOMAIN: &[u8] = b"KEROSENE_ADMIN_AUDIT_REFERENCE_V1";
/// Signature domain for peer-to-peer channel snapshots.
pub const ADMIN_P2P_DOMAIN: &[u8] = b"KEROSENE_ADMIN_P2P_V1";
/// Signature domain for on-ramp order snapshots.
pub const ADMIN_ONRAMP_DOMAIN: &[u8] = b"KEROSENE_ADMIN_ONRAMP_V1";
/// Signature domain for reconciliation snapshots.
pub const ADMIN_RECONCILIATION_DOMAIN: &[u8] = b"KEROSENE_ADMIN_RECONCILIATION_V1";
/// Signature domain for provider health snapshots.
pub const ADMIN_PROVIDER_DOMAIN: &[u8] = b"KEROSENE_ADMIN_PROVIDER_V1";

/// Versioned administrative error response with a stable code and structured details.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdminErrorEnvelopeV1 {
    /// Response schema version.
    pub contract_version: String,
    /// Stable public error code.
    pub code: String,
    /// Safe client-facing error explanation.
    pub message: String,
    /// Request correlation ID.
    pub request_id: String,
    /// Additional structured, non-secret context.
    pub details: serde_json::Value,
}

/// Correlation reference to an immutable audit event associated with a request.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AuditReferenceV1 {
    /// Persisted audit event ID.
    pub event_id: String,
    /// Associated request ID.
    pub request_id: String,
    /// Audit event timestamp.
    pub occurred_at: String,
}

/// Operational snapshot of a node membership plane and its readiness gates.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct NodeAdminStatusV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Identifier of the distributed network this record belongs to.
    pub network_id: String,
    /// Membership or operational plane associated with this record.
    pub plane: DiscoveryPlane,
    /// Whether this process passed its local readiness checks.
    pub local_ready: bool,
    /// Whether the node is admitted and eligible as a network member.
    pub member_ready: bool,
    /// Whether enough members are available to satisfy the configured quorum.
    pub quorum_ready: bool,
    /// Whether financial operations can be served safely.
    pub financial_ready: bool,
    /// Number of members currently considered live by the node.
    pub live_members: u64,
    /// Configured quorum threshold.
    pub threshold: u16,
}

/// Operational snapshot of vault readiness and Bitcoin ceremony configuration.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct VaultAdminStatusV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Local process readiness.
    pub local_ready: bool,
    /// Financial-operation readiness.
    pub financial_ready: bool,
    /// Stable identifier of the vault node.
    pub node_id: String,
    /// Configured distributed key-ceremony protocol or operating mode.
    pub ceremony_mode: String,
    /// Bitcoin network used for wallet derivation and transaction validation.
    pub bitcoin_network: String,
}

/// Administrative snapshot of a peer-to-peer Lightning channel.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdminP2PV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Stable identifier of the Lightning channel.
    pub channel_id: String,
    /// Public node identifier of the remote channel peer.
    pub remote_node_id: String,
    /// Total channel capacity in satoshis.
    pub capacity_sats: u64,
    /// Channel balance on the local side, in satoshis.
    pub local_balance_sats: u64,
    /// Channel balance on the remote side, in satoshis.
    pub remote_balance_sats: u64,
    /// Whether the channel is active.
    pub is_active: bool,
}

/// Administrative snapshot of a fiat on-ramp order and its credited Bitcoin amount.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdminOnrampV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Provider or platform identifier of the on-ramp order.
    pub order_id: String,
    /// Platform user associated with the order.
    pub user_id: String,
    /// Fiat currency code used for the purchase.
    pub fiat_currency: String,
    /// Fiat amount represented as text to preserve decimal precision.
    pub fiat_amount: String,
    /// Bitcoin amount credited or expected, in satoshis.
    pub sats: u64,
    /// External provider responsible for the order.
    pub provider: String,
    /// Current lifecycle or outcome state.
    pub status: String,
    /// Order creation time.
    pub created_at: String,
}

/// Comparison of ledger, on-chain, and Lightning balances for a reconciliation run.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdminReconciliationV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Stable identifier for the reconciliation run.
    pub reconciliation_id: String,
    /// Ledger total included in the comparison, in satoshis.
    pub ledger_sats: u64,
    /// Observed on-chain balance included in the comparison, in satoshis.
    pub onchain_sats: u64,
    /// Observed Lightning balance included in the comparison, in satoshis.
    pub lightning_sats: u64,
    /// Signed reconciliation difference.
    pub delta_sats: i64,
    /// Current lifecycle or outcome state.
    pub status: String,
    /// Time at which the comparison was completed.
    pub reconciled_at: String,
}

/// Administrative health and identity snapshot for an external provider.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct AdminProviderV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Correlation identifier for the administrative request.
    pub request_id: String,
    /// Stable provider instance identifier.
    pub provider_id: String,
    /// Provider category or integration kind.
    pub provider_type: String,
    /// Whether the provider currently reports availability.
    pub is_online: bool,
    /// Last provider health timestamp.
    pub last_heartbeat: String,
    /// Provider or wire implementation version.
    pub version: String,
}

impl CanonicalSignable for NodeAdminStatusV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_NODE_STATUS_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.network_id.as_bytes());
        field(&mut out, self.plane.as_str().as_bytes());
        field(&mut out, if self.local_ready { b"true" } else { b"false" });
        field(&mut out, if self.member_ready { b"true" } else { b"false" });
        field(&mut out, if self.quorum_ready { b"true" } else { b"false" });
        field(
            &mut out,
            if self.financial_ready {
                b"true"
            } else {
                b"false"
            },
        );
        integer(&mut out, self.live_members);
        integer(&mut out, u64::from(self.threshold));
        out
    }
}

impl CanonicalSignable for VaultAdminStatusV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_VAULT_STATUS_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, if self.local_ready { b"true" } else { b"false" });
        field(
            &mut out,
            if self.financial_ready {
                b"true"
            } else {
                b"false"
            },
        );
        field(&mut out, self.node_id.as_bytes());
        field(&mut out, self.ceremony_mode.as_bytes());
        field(&mut out, self.bitcoin_network.as_bytes());
        out
    }
}

impl CanonicalSignable for AdminErrorEnvelopeV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_ERROR_ENVELOPE_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.code.as_bytes());
        field(&mut out, self.message.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        let details_json: Vec<u8> = sort_value(&self.details).to_string().into_bytes();
        field(&mut out, &details_json);
        out
    }
}

impl CanonicalSignable for AuditReferenceV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_AUDIT_REFERENCE_DOMAIN);
        field(&mut out, self.event_id.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.occurred_at.as_bytes());
        out
    }
}

impl CanonicalSignable for AdminP2PV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_P2P_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.channel_id.as_bytes());
        field(&mut out, self.remote_node_id.as_bytes());
        integer(&mut out, self.capacity_sats);
        integer(&mut out, self.local_balance_sats);
        integer(&mut out, self.remote_balance_sats);
        field(&mut out, if self.is_active { b"true" } else { b"false" });
        out
    }
}

impl CanonicalSignable for AdminOnrampV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_ONRAMP_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.order_id.as_bytes());
        field(&mut out, self.user_id.as_bytes());
        field(&mut out, self.fiat_currency.as_bytes());
        field(&mut out, self.fiat_amount.as_bytes());
        integer(&mut out, self.sats);
        field(&mut out, self.provider.as_bytes());
        field(&mut out, self.status.as_bytes());
        field(&mut out, self.created_at.as_bytes());
        out
    }
}

impl CanonicalSignable for AdminReconciliationV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_RECONCILIATION_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.reconciliation_id.as_bytes());
        integer(&mut out, self.ledger_sats);
        integer(&mut out, self.onchain_sats);
        integer(&mut out, self.lightning_sats);
        let delta = self.delta_sats as u64;
        integer(&mut out, delta);
        field(&mut out, self.status.as_bytes());
        field(&mut out, self.reconciled_at.as_bytes());
        out
    }
}

impl CanonicalSignable for AdminProviderV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_PROVIDER_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.request_id.as_bytes());
        field(&mut out, self.provider_id.as_bytes());
        field(&mut out, self.provider_type.as_bytes());
        field(&mut out, if self.is_online { b"true" } else { b"false" });
        field(&mut out, self.last_heartbeat.as_bytes());
        field(&mut out, self.version.as_bytes());
        out
    }
}
