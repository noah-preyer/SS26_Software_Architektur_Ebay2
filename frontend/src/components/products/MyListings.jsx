// eigene anzeigen verwalten: erstellen, bearbeiten, suchen und sortieren.
// lädt alle produkte und filtert clientseitig nach sellerId, später evtl. ein eigener endpunkt.

import { createSignal, createResource, createEffect, createMemo, Show, For } from "solid-js";

const PAGE_SIZE = 8;
import { getProducts, errorMessage } from "../../lib/api.js";
import { getUserInfo } from "../../lib/auth.js";
import { PRODUCT_SORT_OPTIONS, filterProducts, sortProducts } from "../../lib/productFilters.js";
import ProductForm from "./ProductForm.jsx";
import ProductListItem from "./ProductListItem.jsx";
import SearchSortBar from "./SearchSortBar.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";

export default function MyListings() {
  const userId = () => Number(getUserInfo()?.id);

  const [result, { refetch }] = createResource(() => getProducts());

  const [showForm, setShowForm] = createSignal(false);
  const [editProduct, setEditProduct] = createSignal(null);
  const [editParamHandled, setEditParamHandled] = createSignal(false);

  const myProducts = () => {
    const all = result()?.data ?? [];
    return all.filter((p) => Number(p.sellerId) === userId());
  };

  const [search, setSearch] = createSignal("");
  const [sort, setSort] = createSignal("newest");
  const [page, setPage] = createSignal(1);

  const filtered = createMemo(() =>
    sortProducts(filterProducts(myProducts(), search()), sort())
  );

  const totalPages = createMemo(() => Math.max(1, Math.ceil(filtered().length / PAGE_SIZE)));
  const visible = createMemo(() => filtered().slice((page() - 1) * PAGE_SIZE, page() * PAGE_SIZE));

  // wenn man von der detailseite mit ?edit=id kommt, formular direkt öffnen.
  createEffect(() => {
    const all = result()?.data;
    if (!all || editParamHandled()) return;
    setEditParamHandled(true);

    const params = new URLSearchParams(window.location.search);
    const editId = params.get("edit");
    if (!editId) return;

    const product = all.find((p) => Number(p.id) === Number(editId));
    if (product) {
      setEditProduct(product);
      setShowForm(true);
    }
  });

  const openNew = () => {
    setEditProduct(null);
    setShowForm(true);
  };

  const openEdit = (product) => {
    setEditProduct(product);
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditProduct(null);
    window.history.replaceState({}, "", "/my-listings");
  };

  const handleSuccess = () => {
    closeForm();
    refetch();
  };

  return (
    <div class="pb-8">
      <Show when={!showForm()}>
        <button
          type="button"
          onClick={openNew}
          class="w-full mb-6 py-5 rounded-md border-2 border-dashed border-[#E8400C] text-[#E8400C] font-bold flex items-center justify-center gap-2 transition-all duration-200 hover:bg-[#E8400C] hover:text-white"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.862 4.487z" />
          </svg>
          Neue Anzeige erstellen
        </button>
      </Show>

      <Show when={showForm()}>
        <ProductForm product={editProduct()} onSuccess={handleSuccess} onCancel={closeForm} />
      </Show>

      <Show when={!showForm()}>
        <p class="text-slate-600" classList={{ hidden: !result.loading }}>
          Lädt...
        </p>

        <Show when={result.error}>
          <ErrorBanner type="error" message={errorMessage(result.error)} />
        </Show>

        <Show when={result()?.isMock}>
          <ErrorBanner type="warning" message="Beispieldaten werden angezeigt - Backend nicht erreichbar." />
        </Show>

        <Show when={result() && myProducts().length === 0}>
          <div class="card p-8 text-center">
            <p class="mb-4">Du hast noch keine Anzeigen.</p>
            <button type="button" onClick={openNew} class="btn-primary">
              Erste Anzeige erstellen
            </button>
          </div>
        </Show>

        <Show when={myProducts().length > 0}>
          <SearchSortBar
            placeholder="Anzeigen durchsuchen..."
            onSearch={(v) => { setSearch(v); setPage(1); }}
            sortOptions={PRODUCT_SORT_OPTIONS}
            sortValue={sort()}
            onSort={(v) => { setSort(v); setPage(1); }}
          />

          <Show
            when={visible().length > 0}
            fallback={<p class="text-center text-slate-600 py-8">Keine Anzeigen gefunden.</p>}
          >
            <div class="space-y-4">
              <For each={visible()}>
                {(product) => (
                  <ProductListItem product={product} showStatus>
                    <button type="button" onClick={() => openEdit(product)} class="btn-secondary">
                      Bearbeiten
                    </button>
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
      </Show>
    </div>
  );
}
