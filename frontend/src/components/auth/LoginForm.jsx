// login-formular. wenn das backend nicht erreichbar ist, gibt es einen demo-button für präsentationen.

import { createSignal, Show } from "solid-js";
import { loginUser, errorMessage } from "../../lib/api.js";
import { setSession } from "../../lib/auth.js";
import { mockUsers } from "../../lib/mockData.js";
import ErrorBanner from "../ui/ErrorBanner.jsx";

// redirect nur auf interne pfade, "//..." oder absolute urls werden abgelehnt.
function safeRedirect() {
  if (typeof window === "undefined") return "/";
  const target = new URLSearchParams(window.location.search).get("redirect");
  return target && target.startsWith("/") && !target.startsWith("//") ? target : "/";
}

export default function LoginForm() {
  const [email, setEmail] = createSignal("");
  const [password, setPassword] = createSignal("");
  const [error, setError] = createSignal("");
  const [showDemoButton, setShowDemoButton] = createSignal(false);
  const [loading, setLoading] = createSignal(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setShowDemoButton(false);
    setLoading(true);
    try {
      await loginUser(email(), password());
      window.location.href = safeRedirect();
    } catch (err) {
      setError(errorMessage(err, { 400: "E-Mail oder Passwort falsch." }));
      if (err.kind === "NETWORK" || err.kind === "TIMEOUT") setShowDemoButton(true);
    } finally {
      setLoading(false);
    }
  };

  const continueAsDemo = () => {
    const demoUser = mockUsers.find((u) => u.email === "demo@example.com");
    setSession({ token: "mock-token", user: demoUser });
    window.location.href = "/";
  };

  return (
    <div class="w-full max-w-md">
      <h2 class="font-black text-[#111111] mb-2 tracking-tight" style="font-size: 2.5rem; line-height:1;">
        Anmelden
      </h2>
      <p class="text-[#666] text-sm mb-8">Noch kein Konto?{" "}
        <a href="/register" class="font-bold text-[#E8400C] hover:underline">Registrieren</a>
      </p>

      <form class="space-y-5" onSubmit={handleSubmit}>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="email">E-Mail</label>
          <input
            id="email" type="email" required
            class="input-field"
            value={email()}
            onInput={(e) => setEmail(e.target.value)}
          />
        </div>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="password">Passwort</label>
          <input
            id="password" type="password" required minLength="8"
            class="input-field"
            value={password()}
            onInput={(e) => setPassword(e.target.value)}
          />
        </div>

        <button type="submit" disabled={loading()} class="btn-primary w-full mt-2">
          {loading() ? "Anmelden..." : "Anmelden →"}
        </button>
      </form>

      <Show when={error()}>
        <div class="mt-4 bg-red-50 border-2 border-red-400 rounded-md px-4 py-3 text-sm font-semibold text-red-700">
          {error()}
        </div>
      </Show>

      <Show when={showDemoButton()}>
        <button type="button" onClick={continueAsDemo} class="btn-secondary w-full mt-3">
          Mit Demo-Nutzer fortfahren
        </button>
      </Show>
    </div>
  );
}
