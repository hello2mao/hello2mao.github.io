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

function createDidV2() {
    // gen seed 
    let entropy = randomBytes(32).toString('hex')
    console.log('entropy: ' +entropy);
    const mnemonic = bip39.entropyToMnemonic(entropy)
    console.log('mnemonic: '+mnemonic);
    let seed = bip39.mnemonicToSeedSync(mnemonic).toString('hex')
    console.log('seed: '+seed)

    // HDKey
    let hdkey = HDKey.fromMasterSeed(seed)
    console.log('m: '+hdkey.privateKey)

    console.log('m/0: ' + hdkey.deriveChild(0).privateKey)
    console.log('m/1: ' + hdkey.deriveChild(1).privateKey)
    console.log('m/2: ' + hdkey.deriveChild(2).privateKey)



}

createDidV2()