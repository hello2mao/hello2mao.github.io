---
layout:     post
title:      "解析以太坊地址的生成过程"
subtitle:   "Detail about ethereum account"
date:       2018-05-22 11:38:12
author:     "hello2mao"
tags:
    - ethereum
---

- [一、获得一个以太坊钱包地址](#%E4%B8%80%E8%8E%B7%E5%BE%97%E4%B8%80%E4%B8%AA%E4%BB%A5%E5%A4%AA%E5%9D%8A%E9%92%B1%E5%8C%85%E5%9C%B0%E5%9D%80)
- [二、地址生成解析](#%E4%BA%8C%E5%9C%B0%E5%9D%80%E7%94%9F%E6%88%90%E8%A7%A3%E6%9E%90)
- [三、总结](#%E4%B8%89%E6%80%BB%E7%BB%93)

# 一、获得一个以太坊钱包地址
通过以太坊命令行客户端geth可以很简单的获得一个以太坊地址，如下：

```
[work@host]$ geth account new
INFO [05-22|10:17:57] Maximum peer count                       ETH=25 LES=0 total=25
Your new account is locked with a password. Please give a password. Do not forget this password.
Passphrase:
Repeat passphrase:
Address: {07a78fc0fb8c175d8e09e942086985d2835b6849}
```

地址**0x07a78fc0fb8c175d8e09e942086985d2835b6849**就是新生成的以太坊地址。

# 二、地址生成解析
下面跟踪geth的源码：https://github.com/ethereum/go-ethereum 来分析其地址生成过程。
geth是用https://github.com/urfave/cli 来做命令行解析的，运行geth account new时的入口在cmd/geth/main.go：

```go
func init() {
  // Initialize the CLI app and start Geth
  app.Action = geth
  ...
  app.Commands = []cli.Command{
    ...
    // See accountcmd.go:
    accountCommand,
    ...
  }
  ...
}
```

账户相关的命令在cmd/geth/accountcmd.go里，新建账户命令为new:  

```go
var (
  accountCommand = cli.Command{
    Name:     "account",
    Usage:    "Manage accounts",
    Category: "ACCOUNT COMMANDS",
    Description: ``,
    Subcommands: []cli.Command{
    ...
      {
        Name:   "new",
        Usage:  "Create a new account",
        Action: utils.MigrateFlags(accountCreate),
        Flags: []cli.Flag{
          utils.DataDirFlag,
          utils.KeyStoreDirFlag,
          utils.PasswordFileFlag,
          utils.LightKDFFlag,
        },
        Description: ``,
      },
      ...
    },
  }
)
```

但new一个新账户的时候，会调用accountCreate：  

```text
// accountCreate creates a new account into the keystore defined by the CLI flags.
func accountCreate(ctx *cli.Context) error {
    // （1）获取配置
  cfg := gethConfig{Node: defaultNodeConfig()}
  // Load config file.
  if file := ctx.GlobalString(configFileFlag.Name); file != "" {
    if err := loadConfig(file, &cfg); err != nil {
      utils.Fatalf("%v", err)
    }
  }
  utils.SetNodeConfig(ctx, &cfg.Node)
  scryptN, scryptP, keydir, _ := cfg.Node.AccountConfig()

  // （2）解析用户密码
  password := getPassPhrase("Your new account is locked with a password. Please give a password. Do not forget this password.", true, 0, utils.MakePasswordList(ctx))
  // （3）生成地址
  address, err := keystore.StoreKey(keydir, password, scryptN, scryptP)

  if err != nil {
    utils.Fatalf("Failed to create account: %v", err)
  }
  fmt.Printf("Address: %x\n", address)
  return nil
}
```

分为三步：

 1. 获取配置
 2. 解析用户密码
 3. 生成地址
 
第三步生成地址调用的keystore.StoreKey（accounts/keystore/keystore_passphrase.go）：

```go
// StoreKey generates a key, encrypts with 'auth' and stores in the given directory
func StoreKey(dir, auth string, scryptN, scryptP int) (common.Address, error) {
  _, a, err := storeNewKey(&keyStorePassphrase{dir, scryptN, scryptP}, crand.Reader, auth)
  return a.Address, err
}
```

这边直接调用了storeNewKey（accounts/keystore/key.go）创建新账户：  

```go
func storeNewKey(ks keyStore, rand io.Reader, auth string) (*Key, accounts.Account, error) {
    // 创建一个新的账户
  key, err := newKey(rand)
  if err != nil {
    return nil, accounts.Account{}, err
  }
  a := accounts.Account{Address: key.Address, URL: accounts.URL{Scheme: KeyStoreScheme, Path: ks.JoinPath(keyFileName(key.Address))}}
  if err := ks.StoreKey(a.URL.Path, key, auth); err != nil {
    zeroKey(key.PrivateKey)
    return nil, a, err
  }
  return key, a, err
}

func newKey(rand io.Reader) (*Key, error) {
    // （1）生成公钥和私钥
  privateKeyECDSA, err := ecdsa.GenerateKey(crypto.S256(), rand)
  if err != nil {
    return nil, err
  }
  // （2）由公钥算出地址并构建一个自定义的Key
  return newKeyFromECDSA(privateKeyECDSA), nil
}
```

可以看到，newKey创建新账户时，  

 1. 由secp256k1曲线生成私钥，是由随机的256bit组成
 2. 采用椭圆曲线数字签名算法（ECDSA）将私钥映射成公钥，一个私钥只能映射出一个公钥。
 3. 然后由公钥算出地址并构建一个自定义的Key
 
第三步的代码如下：

```go
func newKeyFromECDSA(privateKeyECDSA *ecdsa.PrivateKey) *Key {
  id := uuid.NewRandom()
  key := &Key{
    Id:         id,
    Address:    crypto.PubkeyToAddress(privateKeyECDSA.PublicKey),
    PrivateKey: privateKeyECDSA,
  }
  return key
}
```

由公钥算出地址是由crypto.PubkeyToAddress（crypto/crypto.go）完成的：

```go
func PubkeyToAddress(p ecdsa.PublicKey) common.Address {
  pubBytes := FromECDSAPub(&p)
  return common.BytesToAddress(Keccak256(pubBytes[1:])[12:])
}

// Keccak256 calculates and returns the Keccak256 hash of the input data.
func Keccak256(data ...[]byte) []byte {
  d := sha3.NewKeccak256()
  for _, b := range data {
    d.Write(b)
  }
  return d.Sum(nil)
}
```

可以看到公钥经过Keccak-256单向散列函数变成了256bit，然后取160bit作为地址。本质上是从256bit的私钥映射到160bit的公共地址。这意味着一个账户可以有不止一个私钥。

# 三、总结
总得来说，以太坊地址的生成过程如下：  
 1. 由secp256k1曲线生成私钥，是由随机的256bit组成  
 2. 采用椭圆曲线数字签名算法（ECDSA）将私钥映射成公钥。  
 3. 公钥经过Keccak-256单向散列函数变成了256bit，然后取160bit作为地址  