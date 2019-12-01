# 交易（Transaction）

## 客户端提交交易

`SignedTransaction`是客户端提交的签名过后的交易，有三个字段：

- `raw_txn`：原始交易内容
- `public_key`：交易提交者的公钥（Ed25519）
- `signature`：交易提交者用私钥的签名

```rust
/// A transaction that has been signed.
///
/// A `SignedTransaction` is a single transaction that can be atomically executed. Clients submit
/// these to validator nodes, and the validator and executor submits these to the VM.
///
/// **IMPORTANT:** The signature of a `SignedTransaction` is not guaranteed to be verified. For a
/// transaction whose signature is statically guaranteed to be verified, see
/// [`SignatureCheckedTransaction`].
#[derive(Clone, Eq, PartialEq, Hash, Serialize, Deserialize, CryptoHasher)]
pub struct SignedTransaction {
    /// The raw transaction
    /// 原始交易内容
    raw_txn: RawTransaction,

    /// Sender's public key. When checking the signature, we first need to check whether this key
    /// is indeed the pre-image of the pubkey hash stored under sender's account.
    /// 交易提交者的公钥（Ed25519）
    public_key: Ed25519PublicKey,

    /// Signature of the transaction that correspond to the public key
    /// 交易提交者用私钥的签名
    signature: Ed25519Signature,
}
```

其中原始交易的结构是`RawTransaction`，包含以下几个：

- `sender`：交易发起者的地址
- `sequence_number`：交易序号
- `payload`：需要执行的交易脚本
- `max_gas_amount`：交易发起者愿意花费的最大gas
- `gas_unit_price`：交易发起者愿意花费的最大gas单价
- `expiration_time`：交易过期时间

```rust
/// RawTransaction is the portion of a transaction that a client signs
#[derive(Clone, Debug, Hash, Eq, PartialEq, Serialize, Deserialize, CryptoHasher)]
pub struct RawTransaction {
    /// Sender's address.
    /// 交易发起者的地址
    sender: AccountAddress,
    // Sequence number of this transaction corresponding to sender's account.
    // 交易发起者的交易序号
    sequence_number: u64,
    // The transaction script to execute.
    // 需要执行的交易脚本
    payload: TransactionPayload,

    // Maximal total gas specified by wallet to spend for this transaction.
    // 交易发起者愿意花费的最大gas
    max_gas_amount: u64,
    // Maximal price can be paid per gas.
    // 交易发起者愿意花费的最大gas单价
    gas_unit_price: u64,
    // Expiration time for this transaction.  If storage is queried and
    // the time returned is greater than or equal to this time and this
    // transaction has not been included, you can be certain that it will
    // never be included.
    // A transaction that doesn't expire is represented by a very large value like
    // u64::max_value().
    #[serde(serialize_with = "serialize_duration")]
    #[serde(deserialize_with = "deserialize_duration")]
    // 交易过期时间
    expiration_time: Duration,
}
```

## TransactionInfo

```rust
/// `TransactionInfo` is the object we store in the transaction accumulator. It consists of the
/// transaction as well as the execution result of this transaction.
#[derive(Clone, Debug, Eq, PartialEq, Serialize, Deserialize, CryptoHasher)]
#[cfg_attr(any(test, feature = "fuzzing"), derive(Arbitrary))]
pub struct TransactionInfo {
    /// The hash of this transaction.
    /// 交易的哈希.
    transaction_hash: HashValue,

    /// The root hash of Sparse Merkle Tree describing the world state at the end of this
    /// transaction.
    /// 此交易执行完后的状态树的根哈希
    state_root_hash: HashValue,

    /// The root hash of Merkle Accumulator storing all events emitted during this transaction.
    /// 事件树的根哈希
    event_root_hash: HashValue,
  

    /// The amount of gas used.
    /// gas消耗
    gas_used: u64,

    /// The major status. This will provide the general error class. Note that this is not
    /// particularly high fidelity in the presence of sub statuses but, the major status does
    /// determine whether or not the transaction is applied to the global state or not.
    /// 主状态
    major_status: StatusCode,
}
```