// login-session verwalten: token und nutzerinfos im sessionstorage (tab-gebunden, kein reload-überleben).

const TOKEN_KEY = "lp_token";
const USER_KEY = "lp_user";

function hasStorage() {
  return typeof window !== "undefined" && !!window.sessionStorage;
}

// jwt payload lesen (keine signaturprüfung, das macht das backend).
// base64url hat andere zeichen als base64 und braucht auffüllung auf vielfaches von 4.
export function decodeToken(token) {
  try {
    const payload = token.split(".")[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

// backend kann {access_token, user}, {accessToken, message} oder nur {token} schicken.
// wenn kein user-block mitgeliefert wird, werden die infos aus dem jwt dekodiert.
export function normalizeLoginResponse(data, emailFallback) {
  const token = data.access_token || data.accessToken || data.token;
  if (!token) return null;

  if (data.user) {
    return { token, user: data.user };
  }

  const payload = decodeToken(token);
  return {
    token,
    user: {
      id: payload?.userId ?? payload?.sub ?? null,
      email: payload?.email ?? emailFallback ?? null,
      username: payload?.username ?? null,
    },
  };
}

export function setSession({ token, user }) {
  if (!hasStorage()) return;
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getToken() {
  if (!hasStorage()) return null;
  return sessionStorage.getItem(TOKEN_KEY);
}

export function getUserInfo() {
  if (!hasStorage()) return null;
  const raw = sessionStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function isLoggedIn() {
  // token-ablauf prüft das backend (401), hier reicht "vorhanden".
  return !!getToken();
}

export function clearSession() {
  if (!hasStorage()) return;
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

export function logout() {
  clearSession();
}

export function authHeader() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}
