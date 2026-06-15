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

// backend kann {access_token, user} oder nur {token} schicken, beide formate werden akzeptiert.
// wenn das backend keinen user-block mitschickt, wird die id aus dem jwt sub ausgelesen.
export function normalizeLoginResponse(data, emailFallback) {
  if (data.access_token) {
    const user = data.user
      ? { ...data.user, id: data.user.id != null ? Number(data.user.id) : null }
      : { id: null, email: emailFallback ?? null, username: null };
    return { token: data.access_token, user };
  }

  // sub muss eine zahl sein. wenn das backend dort eine e-mail speichert, bleibt id null.
  const payload = decodeToken(data.token);
  const subAsId = Number(payload?.sub);
  return {
    token: data.token,
    user: {
      id: payload?.sub != null && Number.isFinite(subAsId) ? subAsId : null,
      email: payload?.email ?? payload?.sub ?? emailFallback ?? null,
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
