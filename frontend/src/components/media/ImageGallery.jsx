// bildgalerie mit thumbnail-navigation für die produktdetailseite.

import { createSignal, For, Show } from "solid-js";

const PLACEHOLDER = "https://picsum.photos/seed/no-image/400/300";

export default function ImageGallery(props) {
  const images = () => (props.images && props.images.length > 0 ? props.images : [PLACEHOLDER]);
  const [active, setActive] = createSignal(0);

  const next = () => setActive((i) => (i + 1) % images().length);
  const prev = () => setActive((i) => (i - 1 + images().length) % images().length);

  return (
    <div class="flex flex-col gap-3">
      {/* Hauptbild */}
      <div class="relative w-full aspect-4/3 overflow-hidden bg-[#F2EFE8] border-2 border-[#111111] shadow-[4px_4px_0_#E8400C]">
        <img src={images()[active()]} alt="Produktbild" class="w-full h-full object-cover" />

        <Show when={images().length > 1}>
          <button
            type="button"
            onClick={prev}
            aria-label="Vorheriges Bild"
            class="absolute top-1/2 left-2 -translate-y-1/2 bg-white border-2 border-[#111111] text-[#111111] hover:bg-[#111111] hover:text-white p-1.5 transition-colors"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <button
            type="button"
            onClick={next}
            aria-label="Nächstes Bild"
            class="absolute top-1/2 right-2 -translate-y-1/2 bg-white border-2 border-[#111111] text-[#111111] hover:bg-[#111111] hover:text-white p-1.5 transition-colors"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </Show>
      </div>

      {/* Thumbnails – horizontaler Streifen unten links */}
      <Show when={images().length > 1}>
        <div class="flex gap-2">
          <For each={images()}>
            {(url, i) => (
              <button
                type="button"
                onClick={() => setActive(i())}
                aria-label={`Bild ${i() + 1} anzeigen`}
                class={`w-16 h-16 overflow-hidden border-2 shrink-0 transition-opacity ${
                  i() === active()
                    ? "border-[#E8400C]"
                    : "border-[#111111] opacity-40 hover:opacity-100"
                }`}
              >
                <img src={url} alt="" class="w-full h-full object-cover" />
              </button>
            )}
          </For>
        </div>
      </Show>
    </div>
  );
}
