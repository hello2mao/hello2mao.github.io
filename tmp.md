---
layout: post
title: "Libra源码分析：账号创建流程"
subtitle: "Libra Account Workflow"
date: 2019-12-03 20:29:11
author: "hello2mao"
tags:
  - blockchain
---

<!-- TOC -->

- [1. 引言](#1-%e5%bc%95%e8%a8%80)
- [2. 客户端](#2-%e5%ae%a2%e6%88%b7%e7%ab%af)
  - [2.1. 启动客户端](#21-%e5%90%af%e5%8a%a8%e5%ae%a2%e6%88%b7%e7%ab%af)
  - [2.2. CLI 创建账户](#22-cli-%e5%88%9b%e5%bb%ba%e8%b4%a6%e6%88%b7)
  - [2.3. client入口](#23-client%e5%85%a5%e5%8f%a3)
  - [2.4. account命令的执行](#24-account%e5%91%bd%e4%bb%a4%e7%9a%84%e6%89%a7%e8%a1%8c)
  - [2.5. create命令的执行](#25-create%e5%91%bd%e4%bb%a4%e7%9a%84%e6%89%a7%e8%a1%8c)
  - [2.6. GRPC Client](#26-grpc-client)
- [3. 服务端（Validator节点）](#3-%e6%9c%8d%e5%8a%a1%e7%ab%afvalidator%e8%8a%82%e7%82%b9)
  - [3.1. Validator入口](#31-validator%e5%85%a5%e5%8f%a3)
  - [3.2. AC UpdateToLatestLedger](#32-ac-updatetolatestledger)
  - [3.3. storage-client](#33-storage-client)
  - [3.4. storage-service](#34-storage-service)
  - [3.5. LibraDB](#35-libradb)

<!-- /TOC -->

# 1. 引言

Libra是facebook发起的一个区块链项目，其使命是建立一套简单的、无国界的货币和为数十亿人服务的金融基础设施。

开发者上手Libra，第一件事就是创建一个自己的Libra账户。本文通过分析源码解析了账号的创建流程，为大家打通客户端与验证节点（Validator）之间的交互过程。

# 2. 客户端

## 2.1. 启动客户端

目前有两个方式可以启动客户端并连接到验证节点上。

（1）方式一：直接启动客户端连接在Libra的官方测试网上  

命令如下：
```shell
sh scripts/cli/start_cli_testnet.sh
```

(2)方式二：在本地启动自己的验证节点，并启动客户端连接上去。

命令如下：
```shell
cargo run -p libra-swarm -- -s
```

## 2.2. CLI 创建账户

通过CLI命令`account create`创建新账户，地址为：`20928f6ee91b58415e0a81aee2ba57a7aeb68ee3eebef3cc2e5c6eb6c12fa4fc`，如下所示：

```
libra% account create
>> Creating/retrieving next account from wallet
Created/retrieved account #0 address 20928f6ee91b58415e0a81aee2ba57a7aeb68ee3eebef3cc2e5c6eb6c12fa4fc
```

通过命令`account list`，查看账户的`status`是`Local`，`sequence number`是0

```
libra% account list
User account index: 0, address: 20928f6ee91b58415e0a81aee2ba57a7aeb68ee3eebef3cc2e5c6eb6c12fa4fc, sequence number: 0, status: Local
```

## 2.3. client入口

client的入口在`/client/src/main.rs`的`main`函数中：

```rust
    // 注：省略了部分代码
    loop {
        let readline = rl.readline("libra% ");
        match readline {
            Ok(line) => {
                // 命令解析
                let params = parse_cmd(&line);
                if params.is_empty() {
                    continue;
                }
                // 命令实现的匹配获取
                match alias_to_cmd.get(&params[0]) {
                    Some(cmd) => {
                        if args.verbose {
                            println!("{}", Utc::now().to_rfc3339_opts(SecondsFormat::Secs, true));
                        }
                        // 命令的执行
                        cmd.execute(&mut client_proxy, &params);
                    }
                }
            }
        }
    }
```

所以当用户在命令行输入`account create`时，实际进入的是这个`loop`，会执行如下逻辑：

1. 命令解析  
命令解析主要是把用户在CLI中输入的命令按照空格分割，`params[0]`为主命令。  
2. 命令实现的匹配获取  
在支持的命令里，获取有用户输入命令的实现实例。  
3. 命令的执行  
运行这个实例的`execute`方法，并返回结果。  

`alias_to_cmd`是client支持的命令的别名列表，在main函数启动的时候初始化的，如下：

```rust
let (commands, alias_to_cmd) = get_commands(args.faucet_account_file.is_some());
```

`get_commands()`方法的实现在`/client/src/commands.rs`中：

```rust
/// Returns all the commands available, as well as the reverse index from the aliases to the
/// commands.
pub fn get_commands(
    include_dev: bool,
) -> (
    Vec<Arc<dyn Command>>,
    HashMap<&'static str, Arc<dyn Command>>,
) {
    let mut commands: Vec<Arc<dyn Command>> = vec![
        Arc::new(AccountCommand {}), // account命令
        Arc::new(QueryCommand {}), // query命令
        Arc::new(TransferCommand {}), // transfer命令
    ];
    if include_dev {
        commands.push(Arc::new(DevCommand {})); // dev命令
    }
    let mut alias_to_cmd = HashMap::new();
    for command in &commands {
        for alias in command.get_aliases() {
            alias_to_cmd.insert(alias, Arc::clone(command));
        }
    }
    (commands, alias_to_cmd)
}
```

可以看到，client共支持四大类命令：

- `account`：账户相关，对应AccountCommand
- `query`：查询相关，对应QueryCommand
- `transfer`：转账相关，对应TransferCommand
- `dev`：本地Move开发相关，对应DevCommand

而这四个命令都实现了同一个`trait`（注：rust中的trait的含义类似于接口）：

```rust
/// Trait to perform client operations.
pub trait Command {
    /// all commands and aliases this command support.
    /// 别名
    fn get_aliases(&self) -> Vec<&'static str>;
    /// string that describes params.
    /// 参数描述
    fn get_params_help(&self) -> &'static str {
        ""
    }
    /// string that describes what the command does.
    /// 描述
    fn get_description(&self) -> &'static str;
    /// code to execute.
    /// 命令的执行
    fn execute(&self, client: &mut ClientProxy, params: &[&str]);
}
```

## 2.4. account命令的执行

`account`命令对于`Command`的实现在`/client/src/account_commands.rs`中：

```rust
/// Major command for account related operations.
pub struct AccountCommand {}

impl Command for AccountCommand {
    // 别名
    fn get_aliases(&self) -> Vec<&'static str> {
        vec!["account", "a"]
    }
    // 描述
    fn get_description(&self) -> &'static str {
        "Account operations"
    }
    // 执行
    fn execute(&self, client: &mut ClientProxy, params: &[&str]) {
        let commands: Vec<Box<dyn Command>> = vec![
            Box::new(AccountCommandCreate {}),
            Box::new(AccountCommandListAccounts {}),
            Box::new(AccountCommandRecoverWallet {}),
            Box::new(AccountCommandWriteRecovery {}),
            Box::new(AccountCommandMint {}),
        ];

        // 执行子命令，例如create、list等
        subcommand_execute(&params[0], commands, client, &params[1..]);
    }
}
```

## 2.5. create命令的执行

在`execute`的时候，初始了`account`的五个子命令，然后调用`subcommand_execute（）`去执行相应的子命令，对于`account create`，就是执行`AccountCommandCreate`的`execute`，如下：

```rust
/// Sub command to create a random account. The account will not be saved on chain.
pub struct AccountCommandCreate {}

impl Command for AccountCommandCreate {
    fn get_aliases(&self) -> Vec<&'static str> {
        vec!["create", "c"]
    }
    fn get_description(&self) -> &'static str {
        "Create an account. Returns reference ID to use in other operations"
    }
    // 对应account create的执行
    fn execute(&self, client: &mut ClientProxy, _params: &[&str]) {
        println!(">> Creating/retrieving next account from wallet");
        match client.create_next_account(true) {
            Ok(account_data) => println!(
                "Created/retrieved account #{} address {}",
                account_data.index,
                hex::encode(account_data.address)
            ),
            Err(e) => report_error("Error creating account", e),
        }
    }
}
```

调用的是`client.create_next_account(true)`，而`client`是在main函数中初始化的：

```rust
let mut client_proxy = ClientProxy::new(
        &args.host,
        args.port.get(),
        &args.validator_set_file,
        &faucet_account_file,
        args.sync,
        args.faucet_server,
        args.mnemonic_file,
    )
    .map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, &format!("{}", e)[..]))?;
```

`ClientProxy`的`create_next_account()`在`/client/src/client_proxy.rs`中，如下：

```rust
/// Returns the account index that should be used by user to reference this account
    pub fn create_next_account(&mut self, sync_with_validator: bool) -> Result<AddressAndIndex> {
        // 调用libra_wallet创建新地址
        let (address, _) = self.wallet.new_address()?;

        // 从服务器上获取该新生成地址的账户信息
        let account_data =
            Self::get_account_data_from_address(&self.client, address, sync_with_validator, None)?;

        Ok(self.insert_account_data(account_data))
    }
```

这个方法做了两件事：

1. 调用`wallet.new_address()`创建一个新地址，此处调用的是`libra_wallet`，通过参考文档可以发现，这是个类似bitcoin的BIP32的分层确定性钱包，只是签名算法使用的是`ed25519`，详见：[libra_wallet](https://github.com/libra/libra/tree/master/client/libra_wallet)
2. 调用`get_account_data_from_address()`从服务器上获取该新生成地址的账户信息。`get_account_data_from_address`简单的调用了`GRPCClient`的`get_account_blob()`，然后对返回的信息封装成 `AccountData`。

`AccountData`结构如下：

```rust
/// Struct used to store data for each created account.  We track the sequence number
/// so we can create new transactions easily
#[derive(Debug, Serialize, Deserialize, PartialEq)]
#[cfg_attr(any(test, feature = "fuzzing"), derive(Clone))]
pub struct AccountData {
    /// Address of the account.
    pub address: AccountAddress,
    /// (private_key, public_key) pair if the account is not managed by wallet.
    pub key_pair: Option<KeyPair<Ed25519PrivateKey, Ed25519PublicKey>>,
    /// Latest sequence number maintained by client, it can be different from validator.
    pub sequence_number: u64,
    /// Whether the account is initialized on chain, cached local only, or status unknown.
    pub status: AccountStatus,
}
```

`account create`打印的`status`和`sequence number`就来自这里。

## 2.6. GRPC Client

接下来看`GRPCClient`的`get_account_blob()`。

`GRPCClient`则是真正与validator通讯的grpc客户端。代码在`/client/src/grpc_client.rs`。

```rust
/// Get the latest account state blob from validator.
    pub(crate) fn get_account_blob(
        &self,
        address: AccountAddress,
    ) -> Result<(Option<AccountStateBlob>, Version)> {
        // 准备请求体
        let req_item = RequestItem::GetAccountState { address };
        // 调用get_with_proof_sync
        let mut response = self.get_with_proof_sync(vec![req_item])?;
        // 解析response
        let account_state_with_proof = response
            .response_items
            .remove(0)
            .into_get_account_state_response()?;

        Ok((
            account_state_with_proof.blob,
            response.ledger_info_with_sigs.ledger_info().version(),
        ))
    }
```

首先准备请求体，可以看到只有`address`。
然后`get_account_blob()`调用`get_with_proof_sync()`：

```rust
/// Sync version of get_with_proof
    pub(crate) fn get_with_proof_sync(
        &self,
        requested_items: Vec<RequestItem>,
    ) -> Result<UpdateToLatestLedgerResponse<Ed25519Signature>> {
        // 异步请求，且wait
        let mut resp: Result<UpdateToLatestLedgerResponse<Ed25519Signature>> =
            self.get_with_proof_async(requested_items.clone())?.wait();
        let mut try_cnt = 0_u64;

        // retry
        while Self::need_to_retry(&mut try_cnt, &resp) {
            resp = self.get_with_proof_async(requested_items.clone())?.wait();
        }

        Ok(resp?)
    }
```

可以看到`get_with_proof_sync()`把同步请求转化为多次retry的异步请求`get_with_proof_async()`。

```rust
fn get_with_proof_async(
        &self,
        requested_items: Vec<RequestItem>,
    ) -> Result<
        impl Future<Item = UpdateToLatestLedgerResponse<Ed25519Signature>, Error = failure::Error>,
    > {
        let req = UpdateToLatestLedgerRequest::new(0, requested_items.clone());
        debug!("get_with_proof with request: {:?}", req);
        let proto_req = req.clone().into();
        let validator_verifier = Arc::clone(&self.validator_verifier);
        let ret = self
            .client
            .update_to_latest_ledger_async_opt(&proto_req, Self::get_default_grpc_call_option())?
            .then(move |get_with_proof_resp| {
                // TODO: Cache/persist client_known_version to work with validator set change when
                // the feature is available.

                let resp = UpdateToLatestLedgerResponse::try_from(get_with_proof_resp?)?;
                resp.verify(validator_verifier, &req)?;
                Ok(resp)
            });
        Ok(ret)
    }
```

在异步请求中，调用的是`update_to_latest_ledger_async_opt()`，这是Libra的AC（Admission Control）模块pb service自动生成的接口。在这个接口中就把请求发给了validator节点。

至此，客户端部分的内容就追踪完毕。

# 3. 服务端（Validator节点）

## 3.1. Validator入口

如下是Validator节点的架构。

![](https://developers.libra.org/docs/assets/illustrations/validator-sequence.svg)


Validator节点唯一对外的入口就是AC准入模块，AC的服务在`/admission_control/admission-control-proto/src/proto/admission_control.proto`中定义，如下：

```
// -----------------------------------------------------------------------------
// ---------------- Service definition
// -----------------------------------------------------------------------------
service AdmissionControl {
  // Public API to submit transaction to a validator.
  rpc SubmitTransaction(SubmitTransactionRequest)
      returns (SubmitTransactionResponse) {}

  // This API is used to update the client to the latest ledger version and
  // optionally also request 1..n other pieces of data.  This allows for batch
  // queries.  All queries return proofs that a client should check to validate
  // the data. Note that if a client only wishes to update to the latest
  // LedgerInfo and receive the proof of this latest version, they can simply
  // omit the requested_items (or pass an empty list)
  rpc UpdateToLatestLedger(
      types.UpdateToLatestLedgerRequest)
      returns (types.UpdateToLatestLedgerResponse) {}
}
```

所以AC模块就提供两个接口：

- `SubmitTransaction`：提交交易，上图流程1到12。
- `UpdateToLatestLedger`：查询账本，只涉及到AC和Storage。

而`UpdateToLatestLedger`就是对应于客户端`update_to_latest_ledger_async_opt()`的处理。

## 3.2. AC UpdateToLatestLedger

在`/admission_control/admission-control-service/admission_control_service.rs`中，`AdmissionControlService`的`update_to_latest_ledger()`方法如下：

```rust
    /// This API is used to update the client to the latest ledger version and optionally also
    /// request 1..n other pieces of data.  This allows for batch queries.  All queries return
    /// proofs that a client should check to validate the data.
    /// Note that if a client only wishes to update to the latest LedgerInfo and receive the proof
    /// of this latest version, they can simply omit the requested_items (or pass an empty list).
    /// AC will not directly process this request but pass it to Storage instead.
    fn update_to_latest_ledger(
        &mut self,
        ctx: grpcio::RpcContext<'_>,
        req: libra_types::proto::types::UpdateToLatestLedgerRequest,
        sink: grpcio::UnarySink<libra_types::proto::types::UpdateToLatestLedgerResponse>,
    ) {
        debug!("[GRPC] AdmissionControl::update_to_latest_ledger");
        let _timer = SVC_COUNTERS.req(&ctx);
        // 调用内部方法update_to_latest_ledger_inner
        let resp = self.update_to_latest_ledger_inner(req);
        provide_grpc_response(resp, ctx, sink);
    }
```

直接调用了`update_to_latest_ledger_inner()`：

```rust
/// Pass the UpdateToLatestLedgerRequest to Storage for read query.
    fn update_to_latest_ledger_inner(
        &self,
        req: UpdateToLatestLedgerRequest,
    ) -> Result<UpdateToLatestLedgerResponse> {
        let rust_req = libra_types::get_with_proof::UpdateToLatestLedgerRequest::try_from(req)?;
        // 调用storage_read_client，去读storage
        let (
            response_items,
            ledger_info_with_sigs,
            validator_change_events,
            ledger_consistency_proof,
        ) = self
            .storage_read_client
            .update_to_latest_ledger(rust_req.client_known_version, rust_req.requested_items)?;
        let rust_resp = libra_types::get_with_proof::UpdateToLatestLedgerResponse::new(
            response_items,
            ledger_info_with_sigs,
            validator_change_events,
            ledger_consistency_proof,
        );
        Ok(rust_resp.into())
    }
```

`update_to_latest_ledger_inner()`则调用了`storage_read_client`的`update_to_latest_ledger()`方法去读storage。

## 3.3. storage-client

`storage_read_client`是对storage-service的读client。在这里：`/storage/storage-client/src/lib.rs`，`update_to_latest_ledger()`方法如下：

```rust
fn update_to_latest_ledger(
        &self,
        client_known_version: Version,
        requested_items: Vec<RequestItem>,
    ) -> Result<(
        Vec<ResponseItem>,
        LedgerInfoWithSignatures,
        ValidatorChangeEventWithProof,
        AccumulatorConsistencyProof,
    )> {
        // 调用update_to_latest_ledger_async
        block_on(self.update_to_latest_ledger_async(client_known_version, requested_items))
    }
```

而在`update_to_latest_ledger_async()`方法中，则通过grpc的方式调用了storage-service的`UpdateToLatestLedger`。

## 3.4. storage-service

从这里可以看到，Libra的storage模块单独作为一个grpc service对内提供读写服务。

storage-service的接口也用pb定义的，在`/storage/storage-proto/src/proto/storage.proto`中：

```
// -----------------------------------------------------------------------------
// ---------------- Service definition for storage
// -----------------------------------------------------------------------------
service Storage {
    // Write APIs.

    // Persist transactions. Called by Execution when either syncing nodes or
    // committing blocks during normal operation.
    rpc SaveTransactions(SaveTransactionsRequest)
    returns (SaveTransactionsResponse);

    // Read APIs.

    // Used to get a piece of data and return the proof of it. If the client
    // knows and trusts a ledger info at version v, it should pass v in as the
    // client_known_version and we will return the latest ledger info together
    // with the proof that it derives from v.
    rpc UpdateToLatestLedger(
    types.UpdateToLatestLedgerRequest)
    returns (types.UpdateToLatestLedgerResponse);

    // When we receive a request from a peer validator asking a list of
    // transactions for state synchronization, this API can be used to serve the
    // request. Note that the peer should specify a ledger version and all proofs
    // in the response will be relative to this given ledger version.
    rpc GetTransactions(GetTransactionsRequest) returns (GetTransactionsResponse);

    rpc GetAccountStateWithProofByVersion(
    GetAccountStateWithProofByVersionRequest)
    returns (GetAccountStateWithProofByVersionResponse);

    // Returns information needed for libra core to start up.
    rpc GetStartupInfo(GetStartupInfoRequest)
    returns (GetStartupInfoResponse);

    // Returns latest ledger infos per epoch.
    rpc GetEpochChangeLedgerInfos(GetEpochChangeLedgerInfosRequest)
    returns (GetEpochChangeLedgerInfosResponse);
}
```

可以看出storage-service对外提供1个写接口、5个读接口。

我们重点看`UpdateToLatestLedger`这个read api，对应到storage-service中就是`update_to_latest_ledger()`，位置：`/storage/storage-service/src/lib.rs`，如下：

```rust
fn update_to_latest_ledger(
        &mut self,
        ctx: grpcio::RpcContext<'_>,
        req: UpdateToLatestLedgerRequest,
        sink: grpcio::UnarySink<UpdateToLatestLedgerResponse>,
    ) {
        debug!("[GRPC] Storage::update_to_latest_ledger");
        let _timer = SVC_COUNTERS.req(&ctx);
        // 调用内部方法update_to_latest_ledger_inner
        let resp = self.update_to_latest_ledger_inner(req);
        provide_grpc_response(resp, ctx, sink);
    }
```

内部调用了`update_to_latest_ledger_inner()`：

```rust
fn update_to_latest_ledger_inner(
        &self,
        req: UpdateToLatestLedgerRequest,
    ) -> Result<UpdateToLatestLedgerResponse> {
        let rust_req = libra_types::get_with_proof::UpdateToLatestLedgerRequest::try_from(req)?;

        // 调用db的方法
        let (
            response_items,
            ledger_info_with_sigs,
            validator_change_events,
            ledger_consistency_proof,
        ) = self
            .db
            .update_to_latest_ledger(rust_req.client_known_version, rust_req.requested_items)?;

        let rust_resp = libra_types::get_with_proof::UpdateToLatestLedgerResponse {
            response_items,
            ledger_info_with_sigs,
            validator_change_events,
            ledger_consistency_proof,
        };

        Ok(rust_resp.into())
    }
```

内部调用了db的`update_to_latest_ledger()`，这个db，是对`LibraDB`的wrapper。



## 3.5. LibraDB

`LibraDB`是对底层DB存储的封装，所有libra中需要持久化存储的数据入口都在`LibraDB`中，包括读写操作。

其中`update_to_latest_ledger()`在`storage/libradb/src/lib.rs`,如下：

```rust
/// This backs the `UpdateToLatestLedger` public read API which returns the latest
    /// [`LedgerInfoWithSignatures`] together with items requested and proofs relative to the same
    /// ledger info.
    pub fn update_to_latest_ledger(
        &self,
        client_known_version: Version,
        request_items: Vec<RequestItem>,
    ) -> Result<(
        Vec<ResponseItem>,
        LedgerInfoWithSignatures,
        ValidatorChangeEventWithProof,
        AccumulatorConsistencyProof,
    )> {
        error_if_too_many_requested(request_items.len() as u64, MAX_REQUEST_ITEMS)?;

        // Get the latest ledger info and signatures
        let ledger_info_with_sigs = self.ledger_store.get_latest_ledger_info()?;
        let ledger_info = ledger_info_with_sigs.ledger_info();
        let ledger_version = ledger_info.version();

        // Fulfill all request items
        let response_items = request_items
            .into_iter()
            .map(|request_item| match request_item { // 根据request_item的类型，分别做操作
                // 对应account create
                RequestItem::GetAccountState { address } => Ok(ResponseItem::GetAccountState {
                    account_state_with_proof: self.get_account_state_with_proof(
                        address,
                        ledger_version,
                        ledger_version,
                    )?,
                }),
                RequestItem::GetAccountTransactionBySequenceNumber {
                    account,
                    sequence_number,
                    fetch_events,
                } => {
                    let transaction_with_proof = self.get_txn_by_account(
                        account,
                        sequence_number,
                        ledger_version,
                        fetch_events,
                    )?;

                    let proof_of_current_sequence_number = match transaction_with_proof {
                        Some(_) => None,
                        None => Some(self.get_account_state_with_proof(
                            account,
                            ledger_version,
                            ledger_version,
                        )?),
                    };

                    Ok(ResponseItem::GetAccountTransactionBySequenceNumber {
                        transaction_with_proof,
                        proof_of_current_sequence_number,
                    })
                }

                RequestItem::GetEventsByEventAccessPath {
                    access_path,
                    start_event_seq_num,
                    ascending,
                    limit,
                } => {
                    let (events_with_proof, proof_of_latest_event) = self
                        .get_events_by_query_path(
                            &access_path,
                            start_event_seq_num,
                            ascending,
                            limit,
                            ledger_version,
                        )?;
                    Ok(ResponseItem::GetEventsByEventAccessPath {
                        events_with_proof,
                        proof_of_latest_event,
                    })
                }
                RequestItem::GetTransactions {
                    start_version,
                    limit,
                    fetch_events,
                } => {
                    let txn_list_with_proof =
                        self.get_transactions(start_version, limit, ledger_version, fetch_events)?;

                    Ok(ResponseItem::GetTransactions {
                        txn_list_with_proof,
                    })
                }
            })
            .collect::<Result<Vec<_>>>()?;

        // TODO: cache last epoch change version to avoid a DB access in most cases.
        let client_epoch = self.ledger_store.get_epoch(client_known_version)?;
        let current_epoch = if ledger_info.next_validator_set().is_some() {
            ledger_info.epoch() + 1
        } else {
            ledger_info.epoch()
        };
        let validator_change_proof = if client_epoch < current_epoch {
            self.ledger_store
                .get_epoch_change_ledger_infos(client_epoch, ledger_info.version())?
        } else {
            Vec::new()
        };

        let ledger_consistency_proof = self
            .ledger_store
            .get_consistency_proof(client_known_version, ledger_version)?;

        Ok((
            response_items,
            ledger_info_with_sigs,
            ValidatorChangeEventWithProof::new(validator_change_proof),
            ledger_consistency_proof,
        ))
    }
```

这个方法非常长，主要做的事情，就是根据request_item的类型，分别做操作。

`RequestItem`是个枚举类型，定义如下：

```rust
#[derive(Clone, Debug, Eq, PartialEq)]
#[cfg_attr(any(test, feature = "fuzzing"), derive(Arbitrary))]
pub enum RequestItem {
    GetAccountTransactionBySequenceNumber {
        account: AccountAddress,
        sequence_number: u64,
        fetch_events: bool,
    },
    // this can't be the first variant, tracked here https://github.com/AltSysrq/proptest/issues/141
    GetAccountState {
        address: AccountAddress,
    },
    GetEventsByEventAccessPath {
        access_path: AccessPath,
        start_event_seq_num: u64,
        ascending: bool,
        limit: u64,
    },
    GetTransactions {
        start_version: Version,
        limit: u64,
        fetch_events: bool,
    },
}
```

对应`account create`的是`GetAccountState`，只有一个请求体，即`address`。
在上面方法中调用了`get_account_state_with_proof()`

```rust
    // ================================== Public API ==================================
    /// Returns the account state corresponding to the given version and account address with proof
    /// based on `ledger_version`
    fn get_account_state_with_proof(
        &self,
        address: AccountAddress,
        version: Version,
        ledger_version: Version,
    ) -> Result<AccountStateWithProof> {
        ensure!(
            version <= ledger_version,
            "The queried version {} should be equal to or older than ledger version {}.",
            version,
            ledger_version
        );
        let latest_version = self.get_latest_version()?;
        ensure!(
            ledger_version <= latest_version,
            "The ledger version {} is greater than the latest version currently in ledger: {}",
            ledger_version,
            latest_version
        );
        // 调用 ledger_store 的 get_transaction_info_with_proof 获取指定 Version 的 txn_info 和 txn_info_accumulator_proof
        let (txn_info, txn_info_accumulator_proof) = self
            .ledger_store
            .get_transaction_info_with_proof(version, ledger_version)?;
        // 调用 state_store 的 get_account_state_with_proof_by_state_root 获取指定地址和version的account_state_blob 和 sparse_merkle_proof
        let (account_state_blob, sparse_merkle_proof) = self
            .state_store
            .get_account_state_with_proof_by_version(address, version)?;
        // 组装成`AccountStateWithProof`后，返回
        Ok(AccountStateWithProof::new(
            version,
            account_state_blob,
            AccountStateProof::new(txn_info_accumulator_proof, txn_info, sparse_merkle_proof),
        ))
    }
```

从注释可以看到，这是也是storage-service的一个public api。

分别做了以下几个事：

- 调用 ledger_store 的 get_transaction_info_with_proof 获取指定 Version 的 txn_info 和 txn_info_accumulator_proof
- 调用 state_store 的 get_account_state_with_proof_by_state_root 获取指定地址和version的account_state_blob 和 sparse_merkle_proof
- 组装成`AccountStateWithProof`后，返回。

那`ledger_store`和`state_store`是什么呢？ 

这就涉及到Libra的storage模块的设计了。

我们知道Libra底层存储使用的是`RocksDB`，因为它是一种k-v存储，所以在读和写的时候，肯定存在着对上层数据结构的编解码，而这部分就通过`schemadb`实现的。从DB读取出来的数据会被解码到不同含义的逻辑结构中，在`LibraDB`中，就是各种store，例如：`event_store`、`ledger_store`、`state_store`、`system_store`和`transaction_store`。

**此处就通过这些store根据固定的schema读取rocksdb内的账本数据。**

那什么是`AccountStateWithProof`呢？

```rust
#[derive(Clone, Debug, Eq, PartialEq)]
#[cfg_attr(any(test, feature = "fuzzing"), derive(Arbitrary))]
pub struct AccountStateWithProof {
    /// The transaction version at which this account state is seen.
    pub version: Version,
    /// Blob value representing the account state. If this field is not set, it
    /// means the account does not exist.
    pub blob: Option<AccountStateBlob>,
    /// The proof the client can use to authenticate the value.
    pub proof: AccountStateProof,
}
```

`AccountStateWithProof`有三个字段：

- `version`：这个账号初次有交易时，此交易的version。因为在libra中，账号在钱包中创建好后，我们看到的status就是`Local`，只有当这个账号发生过交易后，才会存在于账本中，status也会变为`Persisted`。而此处的version就记录的使这个账号被记录在账本中的那个交易的version。
- `blob`：记录的是账户状态，如果为空，就说明账号在账本中不存在，那么客户端上`account list`时，账户的status就是`Local`。
- `proof`：用户证明账户状态的完整proof。

所以对于`account create`，返回的blob必然是空的，这也解释了`account list`的status是`Local`的原因。

至此，分析完了服务端的流程。



