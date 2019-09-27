---
title: 解析以太坊地址的生成过程
date: 2018-05-22 11:38:12 Z
tags:
- blockchain
layout: post
subtitle: Detail about ethereum account
author: hello2mao
---

- [一、获得一个以太坊钱包地址](#%e4%b8%80%e8%8e%b7%e5%be%97%e4%b8%80%e4%b8%aa%e4%bb%a5%e5%a4%aa%e5%9d%8a%e9%92%b1%e5%8c%85%e5%9c%b0%e5%9d%80)
- [二、地址生成解析](#%e4%ba%8c%e5%9c%b0%e5%9d%80%e7%94%9f%e6%88%90%e8%a7%a3%e6%9e%90)
- [三、总结](#%e4%b8%89%e6%80%bb%e7%bb%93)

# 一、获得一个以太坊钱包地址

通过以太坊命令行客户端 geth 可以很简单的获得一个以太坊地址，如下：

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

下面跟踪 geth 的源码：https://github.com/ethereum/go-ethereum 来分析其地址生成过程。
geth 是用https://github.com/urfave/cli 来做命令行解析的，运行 geth account new 时的入口在 cmd/geth/main.go：

```
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

账户相关的命令在 cmd/geth/accountcmd.go 里，新建账户命令为 new:

```
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

但 new 一个新账户的时候，会调用 accountCreate：

```
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

1.  获取配置
2.  解析用户密码
3.  生成地址

第三步生成地址调用的 keystore.StoreKey（accounts/keystore/keystore_passphrase.go）：

```
// StoreKey generates a key, encrypts with 'auth' and stores in the given directory
func StoreKey(dir, auth string, scryptN, scryptP int) (common.Address, error) {
  _, a, err := storeNewKey(&keyStorePassphrase{dir, scryptN, scryptP}, crand.Reader, auth)
  return a.Address, err
}
```

这边直接调用了 storeNewKey（accounts/keystore/key.go）创建新账户：

```
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

可以看到，newKey 创建新账户时，

1.  由 secp256k1 曲线生成私钥，是由随机的 256bit 组成
2.  采用椭圆曲线数字签名算法（ECDSA）将私钥映射成公钥，一个私钥只能映射出一个公钥。
3.  然后由公钥算出地址并构建一个自定义的 Key

第三步的代码如下：

```
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

由公钥算出地址是由 crypto.PubkeyToAddress（crypto/crypto.go）完成的：

```
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

可以看到公钥经过 Keccak-256 单向散列函数变成了 256bit，然后取 160bit 作为地址。本质上是从 256bit 的私钥映射到 160bit 的公共地址。这意味着一个账户可以有不止一个私钥。

# 三、总结

总得来说，以太坊地址的生成过程如下：

1.  由 secp256k1 曲线生成私钥，是由随机的 256bit 组成
2.  采用椭圆曲线数字签名算法（ECDSA）将私钥映射成公钥。
3.  公钥经过 Keccak-256 单向散列函数变成了 256bit，然后取 160bit 作为地址
