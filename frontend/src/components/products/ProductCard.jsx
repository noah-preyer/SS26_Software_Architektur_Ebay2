// produktkarte für die übersichtsseite mit warenkorb-button.
// kein verkäufername hier, das wäre ein extra-request pro karte (n+1 problem).

import { createSignal, onMount, Show } from "solid-js";
import { formatPrice, formatDate } from "../../lib/format.js";
import { addToCart, isInCart, removeFromCart } from "../../lib/cart.js";

const PLACEHOLDER = "https://picsum.photos/seed/no-image/400/300";

function CartIcon() {
  return (
    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
        d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
      />
    </svg>
  );
}

export default function ProductCard(props) {
  const image = () => props.product.imageUrls?.[0] ?? PLACEHOLDER;
  const isSold = () => props.product.status === "SOLD";

  const [inCart, setInCart] = createSignal(false);
  onMount(() => setInCart(isInCart(props.product.id)));

  const toggleCart = (e) => {
    e.preventDefault();
    if (inCart()) {
      removeFromCart(props.product.id);
      setInCart(false);
    } else {
      addToCart(props.product.id);
      setInCart(true);
    }
  };

  return (
    <div class="card overflow-hidden group">
      <a href={`/product/${props.product.id}`} class="block">
        {/* Bild */}
        <div class="relative aspect-4/3 bg-[#F2EFE8] overflow-hidden">
          <img
            src={image()}
            alt={props.product.title}
            class={`w-full h-full object-cover ${isSold() ? "grayscale opacity-70" : ""}`}
          />

          {/* Verkauft-Badge: kleine Ecke oben rechts, kein großes Overlay */}
          <Show when={isSold()}>
            <span class="absolute top-3 right-3 bg-[#111111] text-white text-[11px] font-black px-2.5 py-1 uppercase tracking-wider rounded-sm border border-[#333]">
              Verkauft
            </span>
          </Show>

          {/* Kategorie-Badge: unten links */}
          <Show when={props.product.category}>
            <span class="absolute bottom-3 left-3 bg-white/95 text-[#111111] text-[11px] font-bold px-2.5 py-1 rounded-sm border border-[#111111]">
              {props.product.category}
            </span>
          </Show>
        </div>

        {/* Titelbereich */}
        <div class="px-4 pt-3 pb-1">
          <h3 class="font-bold text-[#111111] truncate leading-snug">{props.product.title}</h3>
          <p class="text-xs text-[#999] mt-0.5">{formatDate(props.product.createdAt)}</p>
        </div>
      </a>

      {/* Preis + Warenkorb */}
      <div class="px-4 pb-4 flex items-center justify-between gap-3 mt-2">
        <p class="text-xl font-black text-[#111111] tracking-tight">
          {formatPrice(props.product.price)}
        </p>

        <Show
          when={!isSold()}
          fallback={
            <button disabled class="p-2 rounded-sm border-2 border-[#ddd] text-[#ccc] cursor-not-allowed" aria-label="Nicht verfügbar">
              <CartIcon />
            </button>
          }
        >
          <button
            type="button"
            onClick={toggleCart}
            aria-label={inCart() ? "Aus Warenkorb entfernen" : "In den Warenkorb"}
            class={`p-2 rounded-sm border-2 transition-all duration-150 shrink-0 ${
              inCart()
                ? "border-[#111111] bg-[#111111] text-white hover:bg-[#333]"
                : "border-[#111111] text-[#111111] bg-white hover:bg-[#E8400C] hover:border-[#E8400C] hover:text-white"
            }`}
          >
            <CartIcon />
          </button>
        </Show>
      </div>
    </div>
  );
}
