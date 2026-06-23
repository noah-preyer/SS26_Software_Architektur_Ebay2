// produkt-listeneintrag für warenkorb, bestellungen und eigene anzeigen.
// aktionsbuttons kommen als children rein, die karte selbst hat keine logik.

import { Show } from "solid-js";
import { formatPrice, formatDate } from "../../lib/format.js";

const PLACEHOLDER = "https://picsum.photos/seed/no-image/400/300";

export default function ProductListItem(props) {
  const product = () => props.product;
  const image = () => product().imageUrls?.[0] ?? PLACEHOLDER;
  const isSold = () => product().status === "SOLD";
  const href = () => `/product/${product().id}`;

  return (
    <div
      class="card overflow-hidden cursor-pointer"
      onClick={(e) => { if (!e.target.closest("a, button")) window.location.href = href(); }}
    >
      <div class="flex flex-col md:flex-row">
        {/* Bild */}
        <a href={href()} class="block shrink-0 w-full md:w-2/5">
          <div class="aspect-[4/3] overflow-hidden bg-[#F2EFE8] md:border-r-2 md:border-[#111111]">
            <img
              src={image()}
              alt={product().title}
              loading="lazy"
              class={`w-full h-full object-cover ${isSold() ? "grayscale opacity-70" : ""}`}
            />
          </div>
        </a>

        {/* Text */}
        <div class="grow min-w-0 md:relative">
          <div class="flex flex-col md:absolute md:inset-0 p-5">
            <div class="flex items-start justify-between gap-3 shrink-0">
              <Show when={product().category}>
                <span class="text-xs font-black px-3 py-1 bg-white border-2 border-[#111111] rounded-sm uppercase tracking-wider">
                  {product().category}
                </span>
              </Show>
              <Show when={props.showStatus}>
                <span class={`shrink-0 text-xs font-black px-2.5 py-1 rounded-sm uppercase tracking-wider border-2 ${
                  isSold()
                    ? "bg-[#111111] text-white border-[#111111]"
                    : "bg-white text-[#111111] border-[#111111]"
                }`}>
                  {isSold() ? "Verkauft" : "Verfügbar"}
                </span>
              </Show>
              {props.badge}
            </div>

            <a href={href()} class="block shrink-0">
              <h3 class="text-xl sm:text-2xl font-bold leading-snug tracking-tight mt-1 text-[#111111] hover:text-[#E8400C] transition-colors line-clamp-2">
                {product().title}
              </h3>
            </a>

            <Show when={product().createdAt}>
              <time class="block text-[#999] text-sm mt-2 shrink-0">{formatDate(product().createdAt)}</time>
            </Show>

            <Show when={product().description}>
              <div class="mt-3 md:flex-1 md:min-h-0 md:overflow-hidden">
                <p class="text-[#666] text-base leading-relaxed whitespace-pre-line line-clamp-3 md:line-clamp-6">
                  {product().description}
                </p>
              </div>
            </Show>

            <div class="flex items-end justify-between gap-4 mt-auto pt-4 shrink-0">
              <p class="text-2xl font-black text-[#E8400C]">
                {formatPrice(product().price)}
              </p>
              <div class="shrink-0 flex gap-2">{props.children}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
