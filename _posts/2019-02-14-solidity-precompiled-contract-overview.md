---
layout: post
title: "解析Solidity预编译合约的实现"
subtitle: "Solidity precompiled contract overview"
date: 2019-02-14 10:02:11
author: "hello2mao"
tags:
  - blockchain
---

在 Solidity 中存在很多预先编译好的合约（或者说是方法）可供调用，例如 sha256、keccak256 等，本文简单分析下其实现逻辑。

例如有如下测试合约，在测试合约内调用 sha256：

```
pragma solidity ^0.4.24;

contract Sha256Test {
    uint256 time = 123;
    event hashResult(bytes32);

    function calcSha256(string input) public {
        bytes32 id = sha256(input, time);
        emit hashResult(id);
    }
}
```

此合约源码需要经过 solidity 编译器[solc](https://github.com/ethereum/solidity)编译，编译器解析到 sha256 关键字就会插入一段合约调用的逻辑，编译器源码如下：

文件：[github.com/ethereum/solidity/libsolidity/codegen/ExpressionCompiler.cpp](https://github.com/ethereum/solidity/blob/f003696d7e0e4a1bbe884208db1d651c08cfb01c/libsolidity/codegen/ExpressionCompiler.cpp#L825)

```
case FunctionType::Kind::ECRecover:
case FunctionType::Kind::SHA256:
case FunctionType::Kind::RIPEMD160:
{
  _functionCall.expression().accept(*this);
  static map<FunctionType::Kind, u256> const contractAddresses{
    {FunctionType::Kind::ECRecover, 1},
    {FunctionType::Kind::SHA256, 2},
    {FunctionType::Kind::RIPEMD160, 3}
  };
  m_context << contractAddresses.at(function.kind());
  for (unsigned i = function.sizeOnStack(); i > 0; --i)
    m_context << swapInstruction(i);
  appendExternalFunctionCall(function, arguments);
  break;
}
```

所以，当运行到此处时，对于 sha256 会调用地址是 2 的合约的 sha256 方法。那么在地址是 2 的地方的合约是什么时候部署进以太坊网络的呢？

我们知道，通常智能合约的开发流程是用 solidlity 编写逻辑代码，再通过编译器编译元数据，最后再发布到以太坊上。以太坊底层通过 EVM 模块支持合约的执行与调用，调用时根据合约地址获取到代码，生成环境后载入到 EVM 中运行。
![image](https://user-images.githubusercontent.com/8265961/52756872-bdfb1d80-303d-11e9-9076-0b365c1df65d.png)

执行入口定义在 evm.go 中，功能就是组装执行环境（代码，执行人关系，参数等）。

```
    func (evm *EVM) Call(caller ContractRef, addr common.Address, input []byte, gas uint64, value *big.Int) (ret []byte, leftOverGas uint64, err error) {
        if evm.vmConfig.NoRecursion && evm.depth > 0 {
            return nil, gas, nil
        }

        // 合约调用深度检查
        if evm.depth > int(params.CallCreateDepth) {
            return nil, gas, ErrDepth
        }
        // balance 检查
        if !evm.Context.CanTransfer(evm.StateDB, caller.Address(), value) {
            return nil, gas, ErrInsufficientBalance
        }

        var (
            to       = AccountRef(addr)
            //保存当前状态，如果出错，就回滚到这个状态
            snapshot = evm.StateDB.Snapshot()
        )
        if !evm.StateDB.Exist(addr) {
            //创建调用对象的stateObject
            precompiles := PrecompiledContractsHomestead
            if evm.ChainConfig().IsByzantium(evm.BlockNumber) {
                precompiles = PrecompiledContractsByzantium
            }
            if precompiles[addr] == nil && evm.ChainConfig().IsEIP158(evm.BlockNumber) && value.Sign() == 0 {
                return nil, gas, nil
            }
            evm.StateDB.CreateAccount(addr)
        }
        //调用别人合约可能需要花钱
        evm.Transfer(evm.StateDB, caller.Address(), to.Address(), value)

        //创建合约环境
        contract := NewContract(caller, to, value, gas)
        contract.SetCallCode(&addr, evm.StateDB.GetCodeHash(addr), evm.StateDB.GetCode(addr))

        start := time.Now()

        // Capture the tracer start/end events in debug mode
        if evm.vmConfig.Debug && evm.depth == 0 {
            evm.vmConfig.Tracer.CaptureStart(caller.Address(), addr, false, input, gas, value)

            defer func() { // Lazy evaluation of the parameters
                evm.vmConfig.Tracer.CaptureEnd(ret, gas-contract.Gas, time.Since(start), err)
            }()
        }
        //执行操作
        ret, err = run(evm, contract, input)

        // When an error was returned by the EVM or when setting the creation code
        // above we revert to the snapshot and consume any gas remaining. Additionally
        // when we're in homestead this also counts for code storage gas errors.
        if err != nil {
            //错误回滚
            evm.StateDB.RevertToSnapshot(snapshot)
            if err != errExecutionReverted {
                contract.UseGas(contract.Gas)
            }
        }
        return ret, contract.Gas, err
    }
```

类似的函数有四个。

- Call A->B A,B 的环境独立
- CallCode、 和 Call 类似 区别在于 storage 位置不一样
- DelegateCall、 和 CallCode 类似，区别在于 msg.send 不一样
- StaticCall 和 call 相似 只是不能修改状态

Contract 和参数构造完成后调用执行函数，执行函数会检查调用的是否会之前编译好的原生合约，如果是原生合约则调用原生合约，否则调用解释器执行函数运算合约。

```
    // run runs the given contract and takes care of running precompiles with a fallback to the byte code interpreter.
    func run(evm *EVM, contract *Contract, input []byte) ([]byte, error) {
        if contract.CodeAddr != nil {
            precompiles := PrecompiledContractsHomestead
            if evm.ChainConfig().IsByzantium(evm.BlockNumber) {
                precompiles = PrecompiledContractsByzantium
            }
            if p := precompiles[*contract.CodeAddr]; p != nil {
                return RunPrecompiledContract(p, input, contract)
            }
        }
        return evm.interpreter.Run(contract, input)
    }
```

这里所说的原生合约就是指 native Go 写的预编译的合约，在 go-ethereum 中有定义，如下：

```
// PrecompiledContractsByzantium contains the default set of pre-compiled Ethereum
// contracts used in the Byzantium release.
var PrecompiledContractsByzantium = map[common.Address]PrecompiledContract{
  common.BytesToAddress([]byte{1}): &ecrecover{},
  common.BytesToAddress([]byte{2}): &sha256hash{},
  common.BytesToAddress([]byte{3}): &ripemd160hash{},
  common.BytesToAddress([]byte{4}): &dataCopy{},
  common.BytesToAddress([]byte{5}): &bigModExp{},
  common.BytesToAddress([]byte{6}): &bn256Add{},
  common.BytesToAddress([]byte{7}): &bn256ScalarMul{},
  common.BytesToAddress([]byte{8}): &bn256Pairing{},
}
```

这里我们看到了地址是 2 的 sha256，Go 的实现如下：

```
// SHA256 implemented as a native contract.
type sha256hash struct{}

// RequiredGas returns the gas required to execute the pre-compiled contract.
//
// This method does not require any overflow checking as the input size gas costs
// required for anything significant is so high it's impossible to pay for.
func (c *sha256hash) RequiredGas(input []byte) uint64 {
  return uint64(len(input)+31)/32*params.Sha256PerWordGas + params.Sha256BaseGas
}
func (c *sha256hash) Run(input []byte) ([]byte, error) {
  h := sha256.Sum256(input)
  return h[:], nil
}
```

可以看到实际就是使用的 Go 的 sha256 实现的。

参考：

- [precompiles & solidity](https://medium.com/@rbkhmrcr/precompiles-solidity-e5d29bd428c4)
