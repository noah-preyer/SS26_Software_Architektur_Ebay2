// warenkorbseite mit artikelliste und gesamtsumme.

import { createSignal, createResource, onMount, onCleanup, For, Show } from "solid-js";
import { errorMessage } from "../../lib/api.js";
import { getCart, removeFromCart, onCartChange, loadCartItems } from "../../lib/cart.js";
import { formatPrice } from "../../lib/format.js";
import ProductListItem from "../products/ProductListItem.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";


export default function Cart() {
  const [ids, setIds] = createSignal([]);
  const [result] = createResource(ids, loadCartItems);

  onMount(() => {
    setIds(getCart());
    onCleanup(onCartChange(() => setIds(getCart())));
  });

  const handleRemove = (id) => {
    removeFromCart(id);
    setIds(getCart());
  };

  const total = () => (result() ?? []).reduce((sum, p) => sum + Number(p.price), 0);

  return (
    <div class="pb-8">
      <p class="text-[#666]" classList={{ hidden: !result.loading }}>
        Lädt...
      </p>

      <Show when={result.error}>
        <ErrorBanner type="error" message={errorMessage(result.error)} />
      </Show>

      <Show when={!result.loading && (result() ?? []).length === 0}>
        <div class="card p-8 text-center">
          <p class="mb-4">Dein Warenkorb ist leer.</p>
          <a href="/" class="btn-primary">
            Weiter stöbern
          </a>
        </div>
      </Show>

      <Show when={(result() ?? []).length > 0}>
        <div class="space-y-4 mb-6">
          <For each={result()}>
            {(product) => (
              <ProductListItem product={product}>
                <button type="button" onClick={() => handleRemove(product.id)} class="btn-secondary">
                  Entfernen
                </button>
              </ProductListItem>
            )}
          </For>
        </div>

        <div class="card p-4 flex items-center justify-between mb-6">
          <span class="font-black text-[#111111] uppercase tracking-wider text-sm">Gesamt</span>
          <span class="text-2xl font-black text-[#111111]">
            {formatPrice(total())}
          </span>
        </div>

        <a href="/checkout" class="btn-primary inline-block">
          Zur Kasse
        </a>
      </Show>
    </div>
  );
}
