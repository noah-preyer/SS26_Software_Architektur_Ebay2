// kassenseite mit adress- und zahlformular (demo). kauf läuft über POST /products/{id}/buy.

import { createSignal, createResource, onMount, For, Show } from "solid-js";
import { errorMessage, apiFetch } from "../../lib/api.js";
import { getCart, checkout, loadCartItems } from "../../lib/cart.js";
import { getUserInfo, isLoggedIn, authHeader } from "../../lib/auth.js";
import { formatPrice } from "../../lib/format.js";
import ErrorBanner from "../ui/ErrorBanner.jsx";


export default function Checkout() {
  const [ids, setIds] = createSignal([]);
  const [result] = createResource(ids, loadCartItems);

  const [street, setStreet] = createSignal("");
  const [zip, setZip] = createSignal("");
  const [city, setCity] = createSignal("");
  const [payment, setPayment] = createSignal("invoice");

  const [submitting, setSubmitting] = createSignal(false);
  const [orderResult, setOrderResult] = createSignal(null);
  const [errorMsg, setErrorMsg] = createSignal("");

// login wird erst hier verlangt (der kauf braucht das token), onMount setzt den echten stand.
const [loggedIn, setLoggedIn] = createSignal(false);

onMount(async () => {
  setIds(getCart());
  setLoggedIn(isLoggedIn());

  if (isLoggedIn()) {
    const session = getUserInfo();
    if (session?.id) {
      try {
        const headers = { ...authHeader() };
        const userData = await apiFetch(`/user/by-auth/${session.id}`, { headers });
        const addressData = await apiFetch(`/user/${userData.id}/addresses`, { headers });
        const defaultAddr = addressData?.find((a) => a.defaultAddress) ?? addressData?.[0];
        if (defaultAddr) {
          setStreet(`${defaultAddr.street} ${defaultAddr.houseNumber}`.trim());
          setZip(defaultAddr.postalCode ?? "");
          setCity(defaultAddr.city ?? "");
        }
      } catch {
        // addresses are optional, silently ignore
      }
    }
  }
});

  const total = () => (result() ?? []).reduce((sum, p) => sum + Number(p.price), 0);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setErrorMsg("");
    try {
      const res = await checkout();
      setOrderResult(res);
      setIds(getCart());
      if (res.errors.length > 0) {
        setErrorMsg(errorMessage(res.errors[0].error));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div class="py-8 max-w-2xl mx-auto w-full">
      <h1 class="page-title mb-6">Kasse</h1>

      <Show when={orderResult()?.purchased.length > 0}>
        <div class="card p-10 text-center space-y-5">
          <svg viewBox="0 0 80 80" class="w-20 h-20 mx-auto" fill="none">
            <rect x="6" y="6" width="68" height="68" rx="34" fill="#E8400C" />
            <path d="M28 42l8 8 16-16" stroke="#fff" stroke-width="6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <h2 class="text-3xl font-black tracking-tight">Vielen Dank für deinen Kauf!</h2>
          <p class="text-sm text-[#666666]">Deine Bestellung wurde erfolgreich aufgegeben.</p>
          <a href="/my-orders" class="btn-primary inline-block mt-2">Zu meinen Käufen</a>
        </div>
      </Show>

      <Show when={orderResult()?.conflicts.length > 0}>
        <ErrorBanner
          type="warning"
          message={`${orderResult().conflicts.length} Artikel wurde(n) inzwischen anderweitig verkauft und aus deinem Warenkorb entfernt.`}
          dismissible
        />
      </Show>

      <Show when={errorMsg()}>
        <ErrorBanner type="error" message={errorMsg()} dismissible />
      </Show>

      <Show when={ids().length === 0 && !orderResult()}>
        <div class="card p-8 text-center">
          <p class="mb-4">Dein Warenkorb ist leer.</p>
          <a href="/" class="btn-primary">
            Weiter stöbern
          </a>
        </div>
      </Show>

      <Show when={ids().length > 0}>
        <div class="card p-4 mb-6 space-y-2">
          <For each={result()}>
            {(product) => (
              <div class="flex justify-between text-sm gap-4">
                <span class="truncate">{product.title}</span>
                <span class="flex-shrink-0">{formatPrice(product.price)}</span>
              </div>
            )}
          </For>
          <div class="flex justify-between font-bold pt-2 border-t border-my-primary-container-on">
            <span>Gesamt</span>
            <span>{formatPrice(total())}</span>
          </div>
        </div>

        <Show
          when={loggedIn()}
          fallback={
            <div class="card p-6 text-center">
              <p class="mb-2 font-semibold">Fast geschafft!</p>
              <p class="mb-4 text-sm text-my-on-primary-hint">
                Zum Abschließen deiner Bestellung musst du angemeldet sein - dein
                Warenkorb bleibt dabei erhalten.
              </p>
              <a href="/login?redirect=/checkout" class="btn-primary inline-block">
                Anmelden &amp; bestellen
              </a>
            </div>
          }
        >
          <form onSubmit={handleSubmit} class="card p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1">Straße & Hausnummer</label>
            <input
              type="text"
              required
              value={street()}
              onInput={(e) => setStreet(e.target.value)}
              class="input-field"
            />
          </div>
          <div class="flex gap-4">
            <div class="flex-1">
              <label class="block text-sm font-medium mb-1">PLZ</label>
              <input
                type="text"
                required
                value={zip()}
                onInput={(e) => setZip(e.target.value)}
                class="input-field"
              />
            </div>
            <div class="flex-[2]">
              <label class="block text-sm font-medium mb-1">Ort</label>
              <input
                type="text"
                required
                value={city()}
                onInput={(e) => setCity(e.target.value)}
                class="input-field"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Zahlart</label>
            <select value={payment()} onInput={(e) => setPayment(e.target.value)} class="input-field">
              <option value="invoice">Rechnung</option>
              <option value="creditcard">Kreditkarte</option>
              <option value="paypal">PayPal</option>
            </select>
          </div>

          <button type="submit" disabled={submitting()} class="btn-primary w-full">
            {submitting() ? "Bestellung wird abgeschickt..." : "Bestellen"}
          </button>
          </form>
        </Show>
      </Show>

    </div>
  );
}
