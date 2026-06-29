// alle backend-requests laufen hier durch. browser nutzt /api (proxy), ssr spricht das backend direkt an.

import {
  authHeader,
  clearSession,
  normalizeLoginResponse,
  setSession,
} from "./auth.js";
import { mockGetProduct, mockGetProducts } from "./mockData.js";
const BASE_URL = import.meta.env.SSR
  ? (process.env.BACKEND_TARGET ?? "http://localhost:8090")
  : "/api";
const TIMEOUT = 5000;

async function apiFetch(path, options = {}) {
  const url = BASE_URL + path;
  const method = options.method ?? "GET";

  console.log(`[apiFetch] → ${method} ${url}`, options.body ?? "");
  console.log(`[apiFetch] Full URL: ${url}`);

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT);

  let response;
  try {
    response = await fetch(url, { ...options, signal: controller.signal });
  } catch (err) {
    if (err.name === "AbortError") {
      throw Object.assign(new Error("Zeitüberschreitung"), { kind: "TIMEOUT" });
    }
    throw Object.assign(new Error("Netzwerkfehler"), { kind: "NETWORK" });
  } finally {
    clearTimeout(timer);
  }

  console.log(`[apiFetch] ← ${response.status} ${method} ${url}`);

  if (!response.ok) {
    if (response.status === 401) clearSession();

    let body = null;
    try {
      body = await response.json();
    } catch {
    }

    throw Object.assign(new Error(body?.message ?? `HTTP ${response.status}`), {
      status: response.status,
      body,
    });
  }

  if (response.status === 204) return null;
  return response.json();
}

export function errorMessage(err, overrides = {}) {
  if (err?.status != null && overrides[err.status]) {
    return overrides[err.status];
  }

  if (err?.kind === "NETWORK" || err?.kind === "TIMEOUT") {
    return "Service vorübergehend nicht erreichbar.";
  }

  switch (err?.status) {
    case 401:
      return "Sitzung abgelaufen. Bitte neu einloggen.";
    case 404:
      return "Nicht gefunden.";
    case 409:
      return "Dieses Produkt wurde bereits verkauft.";
    case 400:
      return "Ungültige Eingabe.";
    default:
      return "Serverfehler, bitte später erneut versuchen.";
  }
}

function isConnectionError(err) {
  return err?.kind === "NETWORK" || err?.kind === "TIMEOUT" || err?.status >= 500;
}

// bei netzwerkproblemen oder serverfehlern (5xx) kommen beispieldaten zurück.

export async function getProducts(category) {
  const query = category ? `?category=${encodeURIComponent(category)}` : "";
  try {
    const data = await apiFetch(`/products${query}`);
    return { data, isMock: false };
  } catch (err) {
    if (isConnectionError(err)) {
      return { data: mockGetProducts(category), isMock: true };
    }
    throw err;
  }
}

export async function getProduct(id) {
  try {
    const data = await apiFetch(`/products/${id}`);
    return { data, isMock: false };
  } catch (err) {
    if (isConnectionError(err)) {
      return { data: mockGetProduct(id), isMock: true };
    }
    throw err;
  }
}

export async function createProduct(productData) {
  return apiFetch("/products", {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeader() },
    body: JSON.stringify(productData),
  });
}

export async function updateProduct(id, productData) {
  return apiFetch(`/products/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...authHeader() },
    body: JSON.stringify(productData),
  });
}

export async function deleteProduct(id) {
  return apiFetch(`/products/${id}`, {
    method: "DELETE",
    headers: { ...authHeader() },
  });
}

export async function buyProduct(id) {
  return apiFetch(`/products/${id}/buy`, {
    method: "POST",
    headers: { ...authHeader() },
  });
}

// das echte backend muss bei login {access_token, user:{id,email,username}} zurückgeben.
// die user-id im JWT (sub) muss eine zahl sein, sonst kann der user-auth-check ncht funktionieren.
export async function loginUser(emailOrUsername, password) {
  const data = await apiFetch("/auth", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ emailOrUsername, password }),
  });

  const session = normalizeLoginResponse(data, emailOrUsername);
  setSession(session);
  return session.user;
}

export async function registerUser(username, email, password, firstName, lastName) {
  const authUser = await apiFetch("/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });

  try {
    await apiFetch("/user", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        authUserId: authUser.id,
        username,
        email,
        firstName: firstName || null,
        lastName: lastName || null,
        phoneNumber: null,
        profileImageObjectKey: null,
      }),
    });
  } catch {
    console.warn("[apiFetch] User profile creation failed — auth user already exists");
  }
}

// wenn GET /user/{id} fehlt oder fehlschlägt, zeigt die detailseite einfach "unbekannt", kein absturz.
export async function getSeller(sellerId) {
  try {
    return await apiFetch(`/user/${sellerId}`, { headers: { ...authHeader() } });
  } catch {
    return null;
  }
}
