var bs58 = require('bs58')
var CryptoJS = require('crypto-js')
var Buffer =  require('buffer').Buffer
var elliptic = require('elliptic')
var secp256k1 = require('secp256k1/elliptic')
const bip39 = require('bip39')
var HDKey = require('./HDKey')

let ec = new elliptic.ec('secp256k1');

const didPrefix = 'did:ccp:';
const context = 'https://w3id.org/did/v1';

// TODO：Generate an array of any length of random bytes
function randomBytes(n) {
    for (var bytes = []; n > 0; n--)
        bytes.push(Math.floor(Math.random() * 256));
    return Buffer.from(bytes);
}

function genKeyPair() {
    let privKey;
    do {
        privKey = randomBytes(32);
    } while (!secp256k1.privateKeyVerify(privKey));

    console.log('privKey: ' + privKey.toString('hex'));
    const pubKey = secp256k1.publicKeyCreate(privKey, false);
    return {
        privKey: privKey,
        pubKey: pubKey
    };
}

function hexToBytes(hex) {
  for (var bytes = [], c = 0; c < hex.length; c += 2)
      bytes.push(parseInt(hex.substr(c, 2), 16));
  return bytes;
}

function createDid() {
    let mainKeyPair = genKeyPair();
    let recoveryKeyPair = genKeyPair();

    let entropy = randomBytes(32).toString('hex')
    // bip39.setDefaultWordlist('chinese_simplified')
    const mnemonic = bip39.entropyToMnemonic(entropy)
    console.log('mnemonic: '+mnemonic);
    const entropyRecover = bip39.mnemonicToEntropy(mnemonic)
    console.log('entropyRecover: '+entropyRecover.toString('hex'));

    let seed = bip39.mnemonicToSeedSync(mnemonic).toString('hex')
    console.log('seed: '+seed)

    let extendedKey = CryptoJS.HmacSHA512("Bitcoin seed", seed).toString();
    console.log('extended key: '+extendedKey);
    let masterPrivateKey = extendedKey.slice(0, 64);
    let masterPublicKey = secp256k1.publicKeyCreate(Buffer.from(hexToBytes(masterPrivateKey)), false).toString('hex');
    let masterChainCode = extendedKey.slice(64);
    console.log('masterPrivateKey: '+masterPrivateKey);
    console.log('masterPublicKey: '+masterPublicKey);
    console.log('masterChainCode: '+masterChainCode);

    let index = 0;
    var indexBuffer = Buffer.allocUnsafe(4)
    indexBuffer.writeUInt32BE(index, 0)
    let data = Buffer.concat([Buffer.from(hexToBytes(masterPublicKey)), indexBuffer])
    let I = CryptoJS.HmacSHA512(data, masterChainCode).toString();
    let IL = I.slice(0, 64);
    let IR = I.slice(64);

    let childPrivateKey = secp256k1.privateKeyTweakAdd(Buffer.from(hexToBytes(masterPrivateKey)), Buffer.from(hexToBytes(IL))).toString('hex')
    let childPublicKey = secp256k1.publicKeyCreate(Buffer.from(hexToBytes(childPrivateKey)), false).toString('hex');
    let childChainCode = IR
    console.log('childPrivateKey: '+childPrivateKey);
    console.log('childPublicKey: '+childPublicKey);
    console.log('childChainCode: '+childChainCode);


    let baseDocument = {
        '@context': context,
        publicKey: [
            {
                id: '#key-1',
                type: 'Secp256k1',
                publicKeyHex: mainKeyPair.pubKey.toString('hex')
            },
            {
                id: '#key-2',
                type: 'Secp256k1',
                publicKeyHex: recoveryKeyPair.pubKey.toString('hex')
            }
        ],
        authentication: ['#key-1'],
        recovery: ['#key-2']
    };
    let baseDocumentHash = CryptoJS.RIPEMD160(
        CryptoJS.SHA256(JSON.stringify(baseDocument))
    );
    let baseDocumentBuffer = Buffer.from(
        hexToBytes(baseDocumentHash.toString())
    );
    let didString = bs58.encode(baseDocumentBuffer);
    let did = didPrefix + didString;
    console.log('did: '+ did)
    console.log('main pubKey: ' + mainKeyPair.pubKey.toString('hex'))
    console.log('recovery pubKey: ' + recoveryKeyPair.pubKey.toString('hex'))
}

function createDidV2() {
    // gen seed 
    let entropy = randomBytes(32).toString('hex')
    console.log('entropy: ' +entropy);
    const mnemonic = bip39.entropyToMnemonic(entropy)
    console.log('mnemonic: '+mnemonic);
    let seed = bip39.mnemonicToSeedSync(mnemonic).toString('hex')
    console.log('seed: '+seed)

    seed = '1d55ab509aebcb049b37ccd10ff86821ec6755c5957a2d0e62e285c2b1bf1419c1f34c45392e53539e3454394855ee0a49b56826ec2ef5d91aec192d2760af1b'

    // HDKey
    let hdkey = HDKey.fromMasterSeed(seed)
    console.log('privateKey: '+hdkey.privateKey)
    console.log('publicKey: '+hdkey.publicKey)
    console.log('chainCode: '+hdkey.chainCode)
    console.log('privateExtendedKey: ' + hdkey.privateExtendedKey)
    console.log('publicExtendedKey: ' + hdkey.publicExtendedKey)

    let childHDKey = hdkey.deriveChild(0)
    console.log('childHDKey privateKey: '+childHDKey.privateKey)
    console.log('childHDKey publicKey: '+childHDKey.publicKey)
    console.log('childHDKey chainCode: '+childHDKey.chainCode)
    console.log('childHDKey privateExtendedKey: ' + childHDKey.privateExtendedKey)
    console.log('childHDKey publicExtendedKey: ' + childHDKey.publicExtendedKey)

    let childkey = hdkey.derive("m/0/2147483647/1")
    console.log('childkey privateKey: '+childkey.privateKey)
    console.log('childkey publicKey: '+childkey.publicKey)
    console.log('childkey chainCode: '+childkey.chainCode)
    console.log('childkey privateExtendedKey: ' + childkey.privateExtendedKey)
    console.log('childkey publicExtendedKey: ' + childkey.publicExtendedKey)

}

createDidV2()