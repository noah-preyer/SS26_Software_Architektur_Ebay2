import { createSignal, createMemo, onMount, For, Show } from "solid-js";

const PAGE_SIZE = 8;
import { getProduct, getUserOrders, errorMessage } from "../../lib/api.js";
import { getUserInfo } from "../../lib/auth.js";
import { PRODUCT_SORT_OPTIONS, filterProducts, sortProducts } from "../../lib/productFilters.js";
import ProductListItem from "../products/ProductListItem.jsx";
import SearchSortBar from "../products/SearchSortBar.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";
import OrderStatusBadge from "./OrderStatusBadge.jsx";

export default function MyOrders() {
  const [items, setItems] = createSignal(null);
  const [loading, setLoading] = createSignal(true);
  const [error, setError] = createSignal(null);
  const [search, setSearch] = createSignal("");
  const [sort, setSort] = createSignal("newest");
  const [page, setPage] = createSignal(1);

  const filtered = createMemo(() =>
    sortProducts(filterProducts(items() ?? [], search()), sort())
  );

  const totalPages = createMemo(() => Math.max(1, Math.ceil(filtered().length / PAGE_SIZE)));
  const visible = createMemo(() => filtered().slice((page() - 1) * PAGE_SIZE, page() * PAGE_SIZE));

  onMount(async () => {
    setLoading(true);
    const user = getUserInfo();
    if (!user?.id) {
      setItems([]);
      setLoading(false);
      return;
    }

    try {
      const orders = await getUserOrders(user.id);
      const result = [];
      for (const order of orders) {
        for (const item of (order.items ?? [])) {
          try {
            const { data: product } = await getProduct(item.productId);
            if (product) {
              product._orderId = order.id;
              product._quantity = item.quantity;
              result.push(product);
            }
          } catch {}
        }
      }
      setItems(result);
    } catch (err) {
      setError(err);
      setItems([]);
    } finally {
      setLoading(false);
    }
  });

  return (
    <div class="pb-8">
      <Show when={loading()}>
        <p class="text-slate-600">Lädt...</p>
      </Show>

      <Show when={!loading() && error()}>
        <ErrorBanner type="error" message={errorMessage(error())} />
      </Show>

      <Show when={!loading() && !error() && items() && items().length === 0}>
        <div class="card p-8 text-center">
          <p class="mb-4">Du hast noch nichts gekauft.</p>
          <a href="/" class="btn-primary">
            Jetzt stöbern
          </a>
        </div>
      </Show>

      <Show when={!loading() && !error() && items() && items().length > 0}>
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
                  badge={<OrderStatusBadge orderId={product._orderId} />}
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
