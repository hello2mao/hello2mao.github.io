const http = require("http");

const options = {
  hostname: "127.0.0.1",
  port: 3000,
  path: "/did:ion:test:EiBYP2oUXPxauXRISOdZ8dPYvZBfog6bYux9XO9g8cqxcg",
  method: "GET"
};

const req = http.request(options, res => {
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
