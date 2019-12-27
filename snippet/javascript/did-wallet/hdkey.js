var assert = require('assert')
var Buffer = require('buffer').Buffer
var secp256k1 = require('secp256k1/elliptic')
var CryptoJS = require('crypto-js')


var MASTER_SECRET = "DID seed"
var HARDENED_OFFSET = 0x80000000

function HDKey () {
  this.depth = 0
  this.index = 0
  this._privateKey = null
  this._publicKey = null
  this.chainCode = null
}

Object.defineProperty(HDKey.prototype, 'privateKey', {
  get: function () {
    return this._privateKey
  },
  set: function (value) {
    assert(secp256k1.privateKeyVerify(Buffer.from(hexToBytes(value))) === true, 'Invalid private key')

    this._privateKey = value
    this._publicKey = secp256k1.publicKeyCreate(Buffer.from(hexToBytes(value)), false).toString('hex')
  }
})

Object.defineProperty(HDKey.prototype, 'publicKey', {
  get: function () {
    return this._publicKey
  },
  set: function (value) {
    assert(secp256k1.publicKeyVerify(Buffer.from(hexToBytes(value))) === true, 'Invalid public key')

    this._publicKey = secp256k1.publicKeyConvert(Buffer.from(hexToBytes(value)), true) // force compressed point
    this._privateKey = null
  }
})

Object.defineProperty(HDKey.prototype, 'privateExtendedKey', {
  get: function () {
    if (this._privateKey) {
      return this.privateKey + this.chainCode
    } else {
      return null
    }
  }
})

Object.defineProperty(HDKey.prototype, 'publicExtendedKey', {
  get: function () {
    if (this._privateKey) {
      return this.publicKey + this.chainCode
    } else {
      return null
    }
  }
})

HDKey.prototype.derive = function (path) {
  if (path === 'm' || path === 'M' || path === "m'" || path === "M'") {
    return this
  }

  var entries = path.split('/')
  var hdkey = this
  entries.forEach(function (c, i) {
    if (i === 0) {
      assert(/^[mM]{1}/.test(c), 'Path must start with "m" or "M"')
      return
    }

    var hardened = (c.length > 1) && (c[c.length - 1] === "'")
    var childIndex = parseInt(c, 10) // & (HARDENED_OFFSET - 1)
    assert(childIndex < HARDENED_OFFSET, 'Invalid index')
    if (hardened) childIndex += HARDENED_OFFSET

    hdkey = hdkey.deriveChild(childIndex)
  })

  return hdkey
}

HDKey.prototype.deriveChild = function (index) {
  var isHardened = index >= HARDENED_OFFSET
  var indexBuffer = Buffer.allocUnsafe(4)
  indexBuffer.writeUInt32BE(index, 0)

  var data

  if (isHardened) { // Hardened child
    assert(this.privateKey, 'Could not derive hardened child key')

    var pk = Buffer.from(hexToBytes(this.privateKey))
    var zb = Buffer.alloc(1, 0)
    pk = Buffer.concat([zb, pk])

    // data = 0x00 || ser256(kpar) || ser32(index)
    data = Buffer.concat([pk, indexBuffer])
  } else { // Normal child
    // data = serP(point(kpar)) || ser32(index)
    //      = serP(Kpar) || ser32(index)
    data = Buffer.concat([Buffer.from(hexToBytes(this.publicKey)), indexBuffer])
  }

  let I = CryptoJS.HmacSHA512(data.toString('hex'), this.chainCode).toString();
  var IL = I.slice(0, 64)
  var IR = I.slice(64)

  var hd = new HDKey()

  // Private parent key -> private child key
  if (this.privateKey) {
    // ki = parse256(IL) + kpar (mod n)
    try {
      hd.privateKey = secp256k1.privateKeyTweakAdd(Buffer.from(hexToBytes(this.privateKey)), Buffer.from(hexToBytes(IL))).toString('hex')
      // console.log("privateKey: "+ hd.privateKey)
      // throw if IL >= n || (privateKey + IL) === 0
    } catch (err) {
      // In case parse256(IL) >= n or ki == 0, one should proceed with the next value for i
      return this.derive(index + 1)
    }
  // Public parent key -> public child key
  } else {
    // Ki = point(parse256(IL)) + Kpar
    //    = G*IL + Kpar
    try {
      hd.publicKey = secp256k1.publicKeyTweakAdd(Buffer.from(hexToBytes(this.publicKey)), Buffer.from(hexToBytes(IL)), true).toString('hex')
      // throw if IL >= n || (g**IL + publicKey) is infinity
    } catch (err) {
      // In case parse256(IL) >= n or Ki is the point at infinity, one should proceed with the next value for i
      return this.derive(index + 1, isHardened)
    }
  }

  hd.chainCode = IR
  hd.depth = this.depth + 1
  hd.index = index

  return hd
}

HDKey.prototype.sign = function (hash) {
  return secp256k1.sign(hash, this.privateKey).signature
}

HDKey.prototype.verify = function (hash, signature) {
  return secp256k1.verify(hash, signature, this.publicKey)
}

HDKey.prototype.wipePrivateData = function () {
  if (this._privateKey) crypto.randomBytes(this._privateKey.length).copy(this._privateKey)
  this._privateKey = null
  return this
}

HDKey.prototype.toJSON = function () {
  return {
    xpriv: this.privateExtendedKey,
    xpub: this.publicExtendedKey
  }
}

// Creates an hdkey object from a master seed 
HDKey.fromMasterSeed = function (seed) {
  let I = CryptoJS.HmacSHA512(seed, MASTER_SECRET).toString();
  var IL = I.slice(0, 64)
  var IR = I.slice(64)

  var hdkey = new HDKey()
  hdkey.chainCode = IR
  hdkey.privateKey = IL

  return hdkey
}

function hexToBytes(hex) {
  for (var bytes = [], c = 0; c < hex.length; c += 2)
      bytes.push(parseInt(hex.substr(c, 2), 16));
  return bytes;
}

HDKey.HARDENED_OFFSET = HARDENED_OFFSET
module.exports = HDKey