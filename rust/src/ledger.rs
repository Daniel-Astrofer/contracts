//! Financial ledger account and journal wire types.

use crate::canonical::{domain, field, integer, CanonicalSignable};
use serde::{Deserialize, Serialize};

/// Signature domain for ledger account snapshots.
pub const ADMIN_CORE_LEDGER_ACCOUNT_DOMAIN: &[u8] = b"KEROSENE_ADMIN_CORE_LEDGER_ACCOUNT_V1";
/// Signature domain for ledger journal entries.
pub const ADMIN_CORE_LEDGER_JOURNAL_DOMAIN: &[u8] = b"KEROSENE_ADMIN_CORE_LEDGER_JOURNAL_V1";

/// Admin/core: ledger account for financial read operations.
///
/// Uses a multi-saldo satoshi model — all balances are denominated in sats
/// (1 sat = 1e-8 BTC). `state_root` is the hex-encoded SHA-256 of the
/// account's internal state and is opaque to the wire protocol.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct LedgerAccountV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Stable ledger account identifier.
    pub account_id: String,
    /// Account category used to interpret the balance buckets.
    pub account_type: String,
    /// Confirmed satoshi balance available for immediate ledger spending.
    pub available_sats: u64,
    /// Satoshi amount held for in-flight operations.
    pub reserved_sats: u64,
    /// Inbound satoshi amount observed but not fully settled.
    pub pending_incoming_sats: u64,
    /// Outbound satoshi amount initiated but not fully settled.
    pub pending_outgoing_sats: u64,
    /// Bitcoin-chain balance with sufficient confirmations, in satoshis.
    pub confirmed_onchain_sats: u64,
    /// Observed Bitcoin-chain balance that is not yet confirmed, in satoshis.
    pub unconfirmed_onchain_sats: u64,
    /// Amount the platform policy currently permits Kerosene to spend.
    pub spendable_by_kerosene_sats: u64,
    /// Monotonic account-state version used to detect stale snapshots.
    pub state_version: u64,
    /// Opaque account-state SHA-256 root.
    pub state_root: String,
    /// Account labels used for administrative grouping or filtering.
    pub tags: Vec<String>,
    /// Creation timestamp in the contract wire format.
    pub created_at: String,
    /// Most recent update timestamp in the contract wire format.
    pub updated_at: String,
}

/// Admin/core: ledger journal entry for financial reconciliation.
///
/// `amount_sats` replaces the old generic `amount` + `currency` pair.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct LedgerJournalV1 {
    /// Wire-contract version expected by the serializer and consumer.
    pub contract_version: String,
    /// Stable identifier of this immutable journal entry.
    pub entry_id: String,
    /// Stable ledger account identifier.
    pub account_id: String,
    /// Debit or credit direction.
    pub direction: JournalDirection,
    /// Positive amount moved by this journal entry, in satoshis.
    pub amount_sats: u64,
    /// Human-readable explanation of the ledger operation.
    pub description: String,
    /// Domain identifier linking the entry to its originating operation.
    pub reference: String,
    /// Timestamp when the ledger entry was recorded.
    pub recorded_at: String,
}

/// Direction in which a ledger journal entry changes the account.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum JournalDirection {
    /// Subtracts the journal amount from the account.
    Debit,
    /// Adds the journal amount to the account.
    Credit,
}

impl JournalDirection {
    /// Returns the lowercase wire representation for this direction.
    ///
    /// # Returns
    /// The stable value used in canonical signing and JSON.
    pub const fn as_str(self) -> &'static str {
        match self {
            Self::Debit => "debit",
            Self::Credit => "credit",
        }
    }
}

impl CanonicalSignable for LedgerAccountV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_CORE_LEDGER_ACCOUNT_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.account_id.as_bytes());
        field(&mut out, self.account_type.as_bytes());
        integer(&mut out, self.available_sats);
        integer(&mut out, self.reserved_sats);
        integer(&mut out, self.pending_incoming_sats);
        integer(&mut out, self.pending_outgoing_sats);
        integer(&mut out, self.confirmed_onchain_sats);
        integer(&mut out, self.unconfirmed_onchain_sats);
        integer(&mut out, self.spendable_by_kerosene_sats);
        integer(&mut out, self.state_version);
        field(&mut out, self.state_root.as_bytes());
        integer(&mut out, self.tags.len() as u64);
        for tag in &self.tags {
            field(&mut out, tag.as_bytes());
        }
        field(&mut out, self.created_at.as_bytes());
        field(&mut out, self.updated_at.as_bytes());
        out
    }
}

impl CanonicalSignable for LedgerJournalV1 {
    fn signing_bytes(&self) -> Vec<u8> {
        let mut out = domain(ADMIN_CORE_LEDGER_JOURNAL_DOMAIN);
        field(&mut out, self.contract_version.as_bytes());
        field(&mut out, self.entry_id.as_bytes());
        field(&mut out, self.account_id.as_bytes());
        field(&mut out, self.direction.as_str().as_bytes());
        integer(&mut out, self.amount_sats);
        field(&mut out, self.description.as_bytes());
        field(&mut out, self.reference.as_bytes());
        field(&mut out, self.recorded_at.as_bytes());
        out
    }
}
