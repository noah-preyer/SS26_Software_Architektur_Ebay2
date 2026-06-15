// produktübersicht mit kategorie-tabs, suche, sortierung und pagination.

import { createSignal, createResource, createMemo, For, Show } from "solid-js";
import { getProducts, errorMessage } from "../../lib/api.js";
import { KATEGORIEN } from "../../lib/mockData.js";
import { PRODUCT_SORT_OPTIONS, filterProducts, sortProducts } from "../../lib/productFilters.js";
import ProductCard from "./ProductCard.jsx";
import SearchSortBar from "./SearchSortBar.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";

const PAGE_SIZE = 12;

function SkeletonCard() {
  return (
    <div class="card overflow-hidden animate-pulse">
      <div class="aspect-4/3 bg-gray-200" />
      <div class="p-4 space-y-2">
        <div class="h-4 bg-gray-200 rounded w-3/4" />
        <div class="h-5 bg-gray-200 rounded w-1/3" />
      </div>
    </div>
  );
}

export default function ProductGrid() {
  const [category, setCategory] = createSignal("");
  const [search, setSearch] = createSignal("");
  const [sort, setSort] = createSignal("newest");
  const [page, setPage] = createSignal(1);

  // immer ein objekt als source, sonst feuert createResource bei leerem string nicht neu.
  const [result, { refetch }] = createResource(
    () => ({ category: category() }),
    ({ category }) => getProducts(category || undefined)
  );

  const selectCategory = (cat) => {
    setCategory(cat);
    setPage(1);
  };

  const filtered = createMemo(() => {
    const data = result()?.data ?? [];
    return sortProducts(filterProducts(data, search()), sort());
  });

  const totalPages = createMemo(() => Math.max(1, Math.ceil(filtered().length / PAGE_SIZE)));

  const pageItems = createMemo(() => {
    const start = (page() - 1) * PAGE_SIZE;
    return filtered().slice(start, start + PAGE_SIZE);
  });

  const tabClass = (cat) =>
    `px-4 py-2 rounded-md text-sm font-bold border-2 transition-all duration-150 ${
      category() === cat
        ? "bg-[#111111] text-white border-[#111111]"
        : "bg-white text-[#111111] border-[#111111] hover:bg-[#F2EFE8]"
    }`;

  return (
    <div>
      <div class="flex flex-wrap gap-2 mb-6">
        <button type="button" onClick={() => selectCategory("")} class={tabClass("")}>
          Alle
        </button>
        <For each={KATEGORIEN}>
          {(kat) => (
            <button type="button" onClick={() => selectCategory(kat)} class={tabClass(kat)}>
              {kat}
            </button>
          )}
        </For>
      </div>

      <SearchSortBar
        placeholder="Produkte durchsuchen..."
        onSearch={(v) => {
          setSearch(v);
          setPage(1);
        }}
        sortOptions={PRODUCT_SORT_OPTIONS}
        sortValue={sort()}
        onSort={(v) => {
          setSort(v);
          setPage(1);
        }}
      />

      <Show when={result.loading}>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <For each={[1, 2, 3, 4, 5, 6]}>{() => <SkeletonCard />}</For>
        </div>
      </Show>

      <Show when={result.error}>
        <ErrorBanner type="error" message={errorMessage(result.error)} />
        <button type="button" onClick={refetch} class="btn-secondary">
          Erneut versuchen
        </button>
      </Show>

      <Show when={!result.loading && !result.error}>
        <Show when={result()?.isMock}>
          <ErrorBanner type="warning" message="Beispieldaten werden angezeigt - Backend nicht erreichbar." />
        </Show>

        <Show when={pageItems().length === 0}>
          <p class="text-slate-600">Keine Produkte gefunden.</p>
        </Show>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <For each={pageItems()}>{(product) => <ProductCard product={product} />}</For>
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
    </div>
  );
}
