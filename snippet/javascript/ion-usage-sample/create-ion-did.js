var didAuth = require("@decentralized-identity/did-auth-jose");
var https = require("https");

async function createIONDid() {
  // gen key
  const kid = "#key-1";
  const jwkPriv = await didAuth.EcPrivateKey.generatePrivateKey(kid);
  const jwkPub = jwkPriv.getPublicKey();
  jwkPub.defaultSignAlgorithm = "ES256K";

  // load JWK into an EcPrivateKey object
  const privateKey = didAuth.EcPrivateKey.wrapJwk(jwkPriv.kid, jwkPriv);
  // construct the JWS payload
  const body = {
    "@context": "https://w3id.org/did/v1",
    publicKey: [
      {
        id: jwkPub.kid,
        type: "Secp256k1VerificationKey2018",
        publicKeyJwk: jwkPub
      }
    ],
    service: [
      {
        id: "IdentityHub",
        type: "IdentityHub",
        serviceEndpoint: {
          "@context": "schema.identity.foundation/hub",
          "@type": "UserServiceEndpoint",
          instance: ["did:test:hub.id"]
        }
      }
    ]
  };

  // Construct the JWS header
  const header = {
    alg: jwkPub.defaultSignAlgorithm,
    kid: jwkPub.kid,
    operation: "create",
    proofOfWork: "{}"
  };

  // Sign the JWS
  const cryptoFactory = new didAuth.CryptoFactory([
    new didAuth.Secp256k1CryptoSuite()
  ]);
  const jwsToken = new didAuth.JwsToken(body, cryptoFactory);
  const signedBody = await jwsToken.signAsFlattenedJson(privateKey, { header });

  // Print out the resulting JWS to the console in JSON format
  console.log("Request: \n" + JSON.stringify(signedBody));
  console.log("\n");

  const data = JSON.stringify(signedBody);
  var options = {
    host: "beta.ion.microsoft.com",
    port: 443,
    path: "/api/1.0/register",
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Content-Length": data.length
    }
  };
  var req = https.request(options, function(res) {
    // console.log("STATUS: " + res.statusCode);
    // console.log("HEADERS: " + JSON.stringify(res.headers));
    res.setEncoding("utf8");
    res.on("data", function(chunk) {
      console.log("Response: \n" + chunk);
    });
  });
  req.on("error", function(e) {
    console.log("problem with request: " + e.message);
  });
  // write data to request body
  req.write(data);
  req.end;
}

createIONDid();
