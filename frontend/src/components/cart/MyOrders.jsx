// bestellhistorie: ids aus dem sessionstorage, produktdetails werden per api nachgeladen.

import { createSignal, createResource, createMemo, onMount, For, Show } from "solid-js";

const PAGE_SIZE = 8;
import { getProduct, errorMessage } from "../../lib/api.js";
import { getPurchases, getOrderId } from "../../lib/cart.js";
import { PRODUCT_SORT_OPTIONS, filterProducts, sortProducts } from "../../lib/productFilters.js";
import ProductListItem from "../products/ProductListItem.jsx";
import SearchSortBar from "../products/SearchSortBar.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";
import OrderStatusBadge from "./OrderStatusBadge.jsx";

async function loadOrders(ids) {
  const items = [];
  for (const id of ids) {
    try {
      const { data } = await getProduct(id);
      if (data) items.push(data);
    } catch {
    }
  }
  return items;
}

export default function MyOrders() {
  const [ids, setIds] = createSignal([]);
  const [result] = createResource(ids, loadOrders);
  const [search, setSearch] = createSignal("");
  const [sort, setSort] = createSignal("newest");
  const [page, setPage] = createSignal(1);

  const filtered = createMemo(() =>
    sortProducts(filterProducts(result() ?? [], search()), sort())
  );

  const totalPages = createMemo(() => Math.max(1, Math.ceil(filtered().length / PAGE_SIZE)));
  const visible = createMemo(() => filtered().slice((page() - 1) * PAGE_SIZE, page() * PAGE_SIZE));

  onMount(() => setIds(getPurchases()));

  return (
    <div class="pb-8">

      <p class="text-slate-600" classList={{ hidden: !result.loading }}>
        Lädt...
      </p>

      <Show when={result.error}>
        <ErrorBanner type="error" message={errorMessage(result.error)} />
      </Show>

      <Show when={!result.loading && (result() ?? []).length === 0}>
        <div class="card p-8 text-center">
          <p class="mb-4">Du hast noch nichts gekauft.</p>
          <a href="/" class="btn-primary">
            Jetzt stöbern
          </a>
        </div>
      </Show>

      <Show when={(result() ?? []).length > 0}>
        <SearchSortBar
          placeholder="Käufe durchsuchen..."
          onSearch={(v) => { setSearch(v); setPage(1); }}
          sortOptions={PRODUCT_SORT_OPTIONS}
          sortValue={sort()}
          onSort={(v) => { setSort(v); setPage(1); }}
        />

        <Show
          when={visible().length > 0}
          fallback={<p class="text-center text-slate-600 py-8">Keine Käufe gefunden.</p>}
        >
          <div class="space-y-4">
            <For each={visible()}>
              {(product) => (
                <ProductListItem
                  product={product}
                  badge={<OrderStatusBadge orderId={getOrderId(product.id)} />}
                >
                  <a href={`/product/${product.id}`} class="btn-primary">
                    Ansehen
                  </a>
                </ProductListItem>
              )}
            </For>
          </div>

          <Show when={totalPages() > 1}>
            <div class="flex justify-center items-center gap-4 mt-8">
              <button
                type="button"
                disabled={page() <= 1}
                onClick={() => setPage((p) => p - 1)}
                class="btn-secondary disabled:opacity-50"
              >
                Zurück
              </button>
              <span class="text-sm text-slate-600">
                Seite {page()} von {totalPages()}
              </span>
              <button
                type="button"
                disabled={page() >= totalPages()}
                onClick={() => setPage((p) => p + 1)}
                class="btn-secondary disabled:opacity-50"
              >
                Weiter
              </button>
            </div>
          </Show>
        </Show>
      </Show>
    </div>
  );
}
