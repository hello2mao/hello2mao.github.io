const https = require("https");

const options = {
  hostname: "beta.discover.did.microsoft.com",
  port: 443,
  path:
    "/1.0/identifiers/did:ion:test:EiBNbUbOyzSmE66Akhc-6fYoo_A6QPF15VHSRNFLIJgUsw",
  method: "GET"
};

const req = https.request(options, res => {
  // console.log("statusCode:", res.statusCode);
  // console.log("headers:", res.headers);

  res.on("data", d => {
    console.log("Response: \n" + d);
  });
});

req.on("error", e => {
  console.error(e);
});
req.end();
