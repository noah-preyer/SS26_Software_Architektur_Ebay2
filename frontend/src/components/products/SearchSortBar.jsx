// such- und sortierleiste, meldet änderungen per callback nach oben.

import { createSignal, onMount, onCleanup, Show, For } from "solid-js";

const SEARCH_DEBOUNCE_MS = 300;

export default function SearchSortBar(props) {
  const [term, setTerm] = createSignal("");
  const [menuOpen, setMenuOpen] = createSignal(false);
  let debounceTimer;
  let rootRef;

  const handleInput = (e) => {
    const value = e.target.value;
    setTerm(value);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => props.onSearch?.(value), SEARCH_DEBOUNCE_MS);
  };

  const clear = () => {
    setTerm("");
    clearTimeout(debounceTimer);
    props.onSearch?.("");
  };

  const currentLabel = () =>
    props.sortOptions?.find((o) => o.value === props.sortValue)?.label ?? "Sortieren";

  const selectSort = (value) => {
    props.onSort?.(value);
    setMenuOpen(false);
  };

  onMount(() => {
    const onDocClick = (e) => {
      if (rootRef && !rootRef.contains(e.target)) setMenuOpen(false);
    };
    document.addEventListener("click", onDocClick);
    onCleanup(() => {
      document.removeEventListener("click", onDocClick);
      clearTimeout(debounceTimer);
    });
  });

  return (
    <div class="flex flex-col sm:flex-row gap-3 mb-8" ref={rootRef}>
      {/* Suchfeld */}
      <div class="relative grow">
        <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
          <svg class="h-4 w-4 text-[#666]" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" clip-rule="evenodd"
              d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
            />
          </svg>
        </div>
        <input
          type="text"
          value={term()}
          onInput={handleInput}
          placeholder={props.placeholder ?? "Suchen..."}
          class="w-full bg-white border-2 border-[#111111] text-[#111111] py-2.5 pl-11 pr-11 rounded-md text-sm focus:outline-none focus:border-[#E8400C] focus:ring-2 focus:ring-[rgba(232,64,12,0.18)] transition-all duration-200 placeholder:text-[#aaa]"
        />
        <Show when={term()}>
          <button
            type="button"
            class="absolute inset-y-0 right-0 pr-4 flex items-center"
            onClick={clear}
            aria-label="Suche löschen"
          >
            <svg class="h-4 w-4 text-[#666] hover:text-[#111111] transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </Show>
      </div>

      {/* Sort-Dropdown */}
      <Show when={props.sortOptions?.length}>
        <div class="relative shrink-0">
          <button
            type="button"
            onClick={() => setMenuOpen(!menuOpen())}
            class="flex items-center justify-center gap-2 w-full sm:w-auto bg-white border-2 border-[#111111] rounded-md px-4 py-2.5 text-sm font-bold text-[#111111] hover:bg-[#F2EFE8] transition-colors duration-200"
            aria-haspopup="true"
            aria-expanded={menuOpen()}
          >
            <svg class="h-4 w-4 text-[#111111]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 6h16M4 12h10M4 18h6" />
            </svg>
            <span class="whitespace-nowrap">{currentLabel()}</span>
            <svg
              class={`h-3.5 w-3.5 text-[#666] transition-transform duration-200 ${menuOpen() ? "rotate-180" : ""}`}
              fill="none" viewBox="0 0 24 24" stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          <Show when={menuOpen()}>
            <div class="absolute right-0 mt-1.5 w-52 bg-white border-2 border-[#111111] rounded-md shadow-[4px_4px_0_#111111] py-1 z-50">
              <For each={props.sortOptions}>
                {(opt) => (
                  <button
                    type="button"
                    onClick={() => selectSort(opt.value)}
                    class={`block w-full text-left px-4 py-2 text-sm font-semibold transition-colors ${
                      opt.value === props.sortValue
                        ? "bg-[#E8400C] text-white"
                        : "text-[#111111] hover:bg-[#F2EFE8]"
                    }`}
                  >
                    {opt.label}
                  </button>
                )}
              </For>
            </div>
          </Show>
        </div>
      </Show>
    </div>
  );
}
