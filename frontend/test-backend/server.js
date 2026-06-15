// kleiner test-backend fürs frontend, ohne abhängigkeiten.
// bildet die gateway-routen /auth, /products, /users nach.

import http from "node:http";
import crypto from "node:crypto";
import fs from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import { seedUsers, seedProducts } from "./seed.js";

const PORT = Number(process.env.PORT ?? 8090);
const JWT_SECRET = process.env.JWT_SECRET ?? "test-backend-dev-secret";
const MAX_BODY = 10 * 1024 * 1024; // 10 mb, reicht für ein paar bilder

// zustand wird als json neben dem server gespeichert und beim start geladen.

const DATA_FILE = process.env.DATA_FILE ?? resolve(dirname(fileURLToPath(import.meta.url)), "data.json");
const PERSIST = process.env.PERSIST !== "false";

// laufzeit-zustand
let users;
let products;
let nextUserId;
let nextProductId;

function seedState() {
  users = seedUsers();
  products = seedProducts();
  nextUserId = users.length + 1;
  nextProductId = products.length + 1;
}

function loadState() {
  if (!PERSIST || !fs.existsSync(DATA_FILE)) {
    seedState();
    return;
  }
  try {
    const saved = JSON.parse(fs.readFileSync(DATA_FILE, "utf8"));
    users = saved.users;
    products = saved.products;
    nextUserId = saved.nextUserId;
    nextProductId = saved.nextProductId;
    console.log(`[test-backend] Zustand aus data.json geladen.`);
  } catch (err) {
    console.warn(`[test-backend] data.json unlesbar (${err.message}) - falle auf Seed zurück.`);
    seedState();
  }
}

function persist() {
  if (!PERSIST) return;
  try {
    fs.writeFileSync(DATA_FILE, JSON.stringify({ users, products, nextUserId, nextProductId }));
  } catch (err) {
    console.warn(`[test-backend] Konnte data.json nicht schreiben: ${err.message}`);
  }
}

loadState();

// jwt (hs256, ohne lib)
function b64url(input) {
  return Buffer.from(input).toString("base64url");
}

function signToken(payload) {
  const header = b64url(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = b64url(JSON.stringify(payload));
  const sig = crypto.createHmac("sha256", JWT_SECRET).update(`${header}.${body}`).digest("base64url");
  return `${header}.${body}.${sig}`;
}

function verifyToken(token) {
  try {
    const [header, body, sig] = token.split(".");
    if (!header || !body || !sig) return null;
    const expected = crypto.createHmac("sha256", JWT_SECRET).update(`${header}.${body}`).digest("base64url");
    const a = Buffer.from(sig);
    const b = Buffer.from(expected);
    if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) return null;
    const payload = JSON.parse(Buffer.from(body, "base64url").toString());
    if (payload.exp && Date.now() / 1000 > payload.exp) return null;
    return payload;
  } catch {
    return null;
  }
}

// liest den eingeloggten nutzer aus dem authorization-header.
function authUser(req) {
  const header = req.headers["authorization"] ?? "";
  const match = /^Bearer\s+(.+)$/i.exec(header);
  if (!match) return null;
  const payload = verifyToken(match[1]);
  if (!payload) return null;
  return users.find((u) => u.id === Number(payload.sub)) ?? null;
}

// http-helfer
function send(res, status, body) {
  const json = body === undefined ? "" : JSON.stringify(body);
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
    "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  });
  res.end(json);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY) {
        reject(Object.assign(new Error("Payload too large"), { status: 413 }));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => {
      if (chunks.length === 0) return resolve({});
      try {
        resolve(JSON.parse(Buffer.concat(chunks).toString()));
      } catch {
        reject(Object.assign(new Error("Invalid JSON"), { status: 400 }));
      }
    });
    req.on("error", reject);
  });
}

function publicUser(u) {
  return { id: u.id, username: u.username, email: u.email };
}

// route-handler
async function handleRegister(req, res) {
  const { email, password } = await readBody(req);
  if (!email || !password) {
    return send(res, 400, { message: "Email and password required" });
  }
  if (users.some((u) => u.email === email)) {
    return send(res, 400, { message: "Email already in use" });
  }
  const user = { id: nextUserId++, username: String(email).split("@")[0], email, password };
  users.push(user);
  persist();
  return send(res, 201, { id: user.id, email: user.email });
}

async function handleLogin(req, res) {
  const { email, password } = await readBody(req);
  const user = users.find((u) => u.email === email);
  const invalid = () => send(res, 400, { message: "Invalid credentials" });

  if (!user || !password) return invalid();
  // seed-nutzer (password === null): beliebiges passwort ab 8 zeichen.
  if (user.password == null) {
    if (String(password).length < 8) return invalid();
  } else if (user.password !== password) {
    return invalid();
  }

  const now = Math.floor(Date.now() / 1000);
  const token = signToken({
    sub: user.id,
    email: user.email,
    username: user.username,
    iat: now,
    exp: now + 3600,
  });
  return send(res, 200, {
    access_token: token,
    token_type: "Bearer",
    expires_in: 3600,
    user: publicUser(user),
  });
}

function handleGetProducts(res, url) {
  const category = url.searchParams.get("category");
  const list = category ? products.filter((p) => p.category === category) : products;
  return send(res, 200, list);
}

function handleGetProduct(res, id) {
  const product = products.find((p) => p.id === id);
  if (!product) return send(res, 404, { message: "Product not found" });
  return send(res, 200, product);
}

async function handleCreateProduct(req, res, user) {
  const body = await readBody(req);
  if (!body.title || body.price == null || Number(body.price) <= 0) {
    return send(res, 400, { message: "title and positive price required" });
  }
  const product = {
    id: nextProductId++,
    title: String(body.title),
    description: body.description ?? null,
    price: Number(body.price),
    category: body.category ?? null,
    sellerId: user.id, // kommt aus dem token, nicht vom client
    status: "AVAILABLE",
    imageUrls: Array.isArray(body.imageUrls) ? body.imageUrls : [],
    createdAt: new Date().toISOString(),
  };
  products.push(product);
  persist();
  return send(res, 201, product);
}

async function handleUpdateProduct(req, res, user, id) {
  const product = products.find((p) => p.id === id);
  if (!product) return send(res, 404, { message: "Product not found" });
  if (product.sellerId !== user.id) {
    return send(res, 403, { message: "Not your product" });
  }
  const body = await readBody(req);
  if (body.title !== undefined) product.title = String(body.title);
  if (body.description !== undefined) product.description = body.description;
  if (body.price !== undefined) product.price = Number(body.price);
  if (body.category !== undefined) product.category = body.category;
  if (body.imageUrls !== undefined) {
    product.imageUrls = Array.isArray(body.imageUrls) ? body.imageUrls : [];
  }
  persist();
  return send(res, 200, product);
}

function handleDeleteProduct(res, user, id) {
  const index = products.findIndex((p) => p.id === id);
  if (index === -1) return send(res, 404, { message: "Product not found" });
  if (products[index].sellerId !== user.id) {
    return send(res, 403, { message: "Not your product" });
  }
  products.splice(index, 1);
  persist();
  return send(res, 204);
}

function handleBuyProduct(res, id) {
  const product = products.find((p) => p.id === id);
  if (!product) return send(res, 404, { message: "Product not found" });
  if (product.status === "SOLD") {
    return send(res, 409, { message: "Already sold" });
  }
  product.status = "SOLD";
  persist();
  return send(res, 200, product);
}

function handleGetUser(res, id) {
  const user = users.find((u) => u.id === id);
  if (!user) return send(res, 404, { message: "User not found" });
  return send(res, 200, publicUser(user));
}

// router
const server = http.createServer(async (req, res) => {
  const method = req.method ?? "GET";
  const url = new URL(req.url ?? "/", `http://localhost:${PORT}`);
  const path = url.pathname;

  if (method === "OPTIONS") return send(res, 204);

  try {
    // auth (öffentlich)
    if (method === "POST" && path === "/auth/register") return await handleRegister(req, res);
    if (method === "POST" && path === "/auth/login") return await handleLogin(req, res);

    // produkte lesen (öffentlich)
    if (method === "GET" && path === "/products") return handleGetProducts(res, url);

    const productMatch = /^\/products\/([^/]+)$/.exec(path);
    if (method === "GET" && productMatch) {
      return handleGetProduct(res, Number(productMatch[1]));
    }

    const buyMatch = /^\/products\/([^/]+)\/buy$/.exec(path);

    // produkte schreibend + buy (token nötig)
    if (
      (method === "POST" && path === "/products") ||
      (method === "PUT" && productMatch) ||
      (method === "DELETE" && productMatch) ||
      (method === "POST" && buyMatch)
    ) {
      const user = authUser(req);
      if (!user) return send(res, 401, { message: "Unauthorized" });

      if (method === "POST" && path === "/products") return await handleCreateProduct(req, res, user);
      if (method === "PUT" && productMatch) return await handleUpdateProduct(req, res, user, Number(productMatch[1]));
      if (method === "DELETE" && productMatch) return handleDeleteProduct(res, user, Number(productMatch[1]));
      if (method === "POST" && buyMatch) return handleBuyProduct(res, Number(buyMatch[1]));
    }

    // user
    const userMatch = /^\/users\/([^/]+)$/.exec(path);
    if (method === "GET" && userMatch) return handleGetUser(res, Number(userMatch[1]));

    // health
    if (method === "GET" && (path === "/" || path === "/health")) {
      return send(res, 200, { status: "ok", service: "test-backend", products: products.length });
    }

    return send(res, 404, { message: "Not found" });
  } catch (err) {
    return send(res, err.status ?? 500, { message: err.message ?? "Internal error" });
  }
});

server.listen(PORT, () => {
  console.log(`[test-backend] läuft auf http://localhost:${PORT}`);
  console.log(`[test-backend] ${products.length} Produkte, ${users.length} Nutzer (${PERSIST ? "persistent: data.json" : "in-memory"})`);
  console.log(`[test-backend] Demo-Login: max@example.com / passwort1 (oder anna@/tom@/lisa@/demo@)`);
});
