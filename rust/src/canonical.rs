//! Canonical JSON and deterministic binary signature encodings.

use serde::Serialize;
use sha2::{Digest, Sha256};
use std::collections::BTreeMap;

/// Return the canonical JSON bytes for a serde-serializable value.
///
/// Canonical JSON guarantees:
/// 1. Keys are sorted lexicographically at every nesting level.
/// 2. Output is compact (no whitespace).
/// 3. Integers are serialized as bare numbers.
///
/// These properties are verified by KAT test vectors shared between Rust and
/// Java so that both languages produce identical bytes for the same struct.
pub fn canonical_json_bytes<T: Serialize>(value: &T) -> Vec<u8> {
    let raw = serde_json::to_value(value).unwrap_or(serde_json::Value::Null);
    sort_value(&raw).to_string().into_bytes()
}

/// Recursively sort all JSON object keys using a BTreeMap.
pub fn sort_value(value: &serde_json::Value) -> serde_json::Value {
    match value {
        serde_json::Value::Object(map) => {
            let mut sorted = BTreeMap::new();
            for (k, v) in map {
                sorted.insert(k.clone(), sort_value(v));
            }
            serde_json::Value::Object(sorted.into_iter().collect())
        }
        serde_json::Value::Array(arr) => {
            serde_json::Value::Array(arr.iter().map(sort_value).collect())
        }
        other => other.clone(),
    }
}

/// SHA-256 hash of the canonical JSON bytes.
pub fn canonical_json_hash<T: Serialize>(value: &T) -> String {
    let bytes = canonical_json_bytes(value);
    hex::encode(Sha256::digest(&bytes))
}

/// Encodes a contract into the legacy deterministic binary format used for signatures.
pub trait CanonicalSignable {
    /// Produces the domain-separated signing bytes for this contract value.
    ///
    /// # Returns
    /// Stable bytes whose encoding must remain compatible with existing signatures.
    fn signing_bytes(&self) -> Vec<u8>;
}

/// Hashes the legacy canonical signing bytes of a value.
///
/// # Parameters
/// * `value` - Contract implementing the canonical binary signature encoding.
///
/// # Returns
/// Lowercase hexadecimal SHA-256 digest.
pub fn canonical_hash<T: CanonicalSignable>(value: &T) -> String {
    hex::encode(Sha256::digest(value.signing_bytes()))
}

pub(crate) fn domain(value: &[u8]) -> Vec<u8> {
    let mut out = Vec::with_capacity(256);
    field(&mut out, value);
    out
}

pub(crate) fn integer(out: &mut Vec<u8>, value: u64) {
    out.extend_from_slice(&value.to_be_bytes());
}

pub(crate) fn field(out: &mut Vec<u8>, value: &[u8]) {
    integer(out, value.len() as u64);
    out.extend_from_slice(value);
}
