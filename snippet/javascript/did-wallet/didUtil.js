var  bs58 = require('bs58')
var CryptoJS = require('crypto-js')
var Buffer =  require('buffer').Buffer
var elliptic = require('elliptic')
var secp256k1 = require('secp256k1/elliptic')

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
    let d = new Date();
    let mainKeyPair = genKeyPair();
    let recoveryKeyPair = genKeyPair();

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

createDid();