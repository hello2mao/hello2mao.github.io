# libra-swarm -n 2

```shell
cargo run -- -c ~/libra_2 -l -n 2    

    Finished dev [unoptimized + debuginfo] target(s) in 1.48s
     Running `/Users/mhb/github.com/libra/libra/target/debug/libra-swarm -c /Users/mhb/libra_2 -l -n 2`
Faucet account created in (loaded from) file "/var/folders/tw/zcq5f7lj3hqbsg0n2zgv9jtr0000gn/T/52bb4219ceccb3b078b5889853ee907a/temp_faucet_keys"
Base directory containing logs and configs: Persistent("/Users/mhb/libra_2")
To run the Libra CLI client in a separate process and connect to the validator nodes you just spawned, use this command:
        cargo run --bin client -- -a localhost -p 57651 -s "/Users/mhb/libra_2/0/consensus_peers.config.toml" -m "/var/folders/tw/zcq5f7lj3hqbsg0n2zgv9jtr0000gn/T/52bb4219ceccb3b078b5889853ee907a/temp_faucet_keys"
To run transaction generator run:
        cargo run --bin cluster-test -- --mint-file "/var/folders/tw/zcq5f7lj3hqbsg0n2zgv9jtr0000gn/T/52bb4219ceccb3b078b5889853ee907a/temp_faucet_keys" --swarm --peers "localhost:57651,localhost:57669"  --emit-tx
CTRL-C to exit.
```

```shell
$ ls ~/libra/0 

826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.consensus.keys.toml
826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.network.keys.toml
826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.network_peers.config.toml
826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.seed_peers.toml
consensus_peers.config.toml
genesis.blob
libradb
metrics
node.config.toml
safety_rules.toml
```

## consensus.keys.toml
```
private_key = "727022e7d0b9fc7075f63be0287eb6d127825956664dcb49fd583bee55820fc8"
public_key = "951fcbd7bfe3f8de24fa8f8f4cac7305248e9e643d3d065c1770379b919a32fb"
```

## network.keys.toml
```
[signing_keys]
private_key = "ebf8bd9b42682cfad6b7ba01cfdf08af2c38907caf33b7daf888986d07b5549c"
public_key = "70d328bde1450126f985f7d01c8af639eaae657fba357f3ed84ac865beb4bfdb"

[identity_keys]
private_key = "5026b806e7736e568f67731e8a26d8e581c32f0aaffdc643fbbdab72a1eb5748"
public_key = "fb3d12f9bbf3be4b9e2774dec38d7c36832c770e32583e1d46cdfa2849149a18"
```

## network_peers.config.toml
```
[5181c01f6005d3236cb950f21a83c22489b9f9d1fadc027cf9532d6f99041522]
ns = "b9c92427fb556e509a2afe7cf6d102d4556a4c9748ed0c8d75338fa8b5b38fd1"
ni = "362310b9ce4dc15258ddf5ebe2ddeaad3ab2ca34cb2b3582b94ef61b990d2c61"

[826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a]
ns = "70d328bde1450126f985f7d01c8af639eaae657fba357f3ed84ac865beb4bfdb"
ni = "fb3d12f9bbf3be4b9e2774dec38d7c36832c770e32583e1d46cdfa2849149a18"
```

## seed_peers.toml
```
[seed_peers]
826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a = ["/ip4/0.0.0.0/tcp/57667"]
5181c01f6005d3236cb950f21a83c22489b9f9d1fadc027cf9532d6f99041522 = ["/ip4/0.0.0.0/tcp/57685"]
```

## consensus_peers.config.toml
```
[5181c01f6005d3236cb950f21a83c22489b9f9d1fadc027cf9532d6f99041522]
c = "edf3fca3dd1cb6531a172ac978cc8b272dc00df88fa89e63e69fddd34f82777b"

[826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a]
c = "951fcbd7bfe3f8de24fa8f8f4cac7305248e9e643d3d065c1770379b919a32fb"
```

## node.config.toml
```
[admission_control]
address = "0.0.0.0"
admission_control_service_port = 57651
need_to_check_mempool_before_validation = false
max_concurrent_inbound_syncs = 100

[admission_control.upstream_proxy_timeout]
secs = 1
nanos = 0

[base]
data_dir = "/Users/mhb/libra_2/0"
role = "validator"

[consensus]
max_block_size = 100
proposer_type = "multiple_ordered_proposers"
contiguous_rounds = 2
consensus_keypair_file = "826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.consensus.keys.toml"
consensus_peers_file = "consensus_peers.config.toml"
[consensus.safety_rules.backend]
type = "on_disk_storage"
default = true
path = "safety_rules.toml"

[debug_interface]
admission_control_node_debug_port = 57653
storage_node_debug_port = 57655
metrics_server_port = 57657
public_metrics_server_port = 57659
address = "0.0.0.0"

[execution]
address = "localhost"
port = 57661
genesis_file_location = "genesis.blob"

[logger]
is_async = true
chan_size = 256

[metrics]
dir = "metrics"
collection_interval_ms = 1000

[mempool]
broadcast_transactions = true
shared_mempool_tick_interval_ms = 50
shared_mempool_batch_size = 100
shared_mempool_max_concurrent_inbound_syncs = 100
capacity = 1000000
capacity_per_user = 100
system_transaction_timeout_secs = 86400
system_transaction_gc_interval_ms = 180000
mempool_service_port = 57663
address = "localhost"

[state_sync]
chunk_limit = 250
tick_interval_ms = 100
long_poll_timeout_ms = 30000
max_chunk_limit = 1000
max_timeout_ms = 120000
upstream_peers = []

[storage]
address = "localhost"
port = 57665
dir = "libradb/db"
grpc_max_receive_len = 100000000
[test.account_keypair]
private_key = "029a2f3945a8f6bb1fb5b11a54283a40526d282359243905f1a30db82a9e597c"
public_key = "aebc586242fcd84120cc845bba84b027a62fd6dd5667b737c76e6a4b2453b87f"

[validator_network]
peer_id = "826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a"
listen_address = "/ip4/0.0.0.0/tcp/57667"
advertised_address = "/ip4/0.0.0.0/tcp/57667"
discovery_interval_ms = 1000
connectivity_check_interval_ms = 5000
enable_encryption_and_authentication = true
is_permissioned = true
network_keypairs_file = "826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.network.keys.toml"
network_peers_file = "826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.network_peers.config.toml"
seed_peers_file = "826b443135b47a8bfdf090f0088a427aeae1de70c6cf833823715c07539bcc3a.seed_peers.toml"
[vm_config.publishing_options]
type = "Open"
```

## safety_rules.toml
```
epoch = 1
last_voted_round = 1737
preferred_round = 1736
```