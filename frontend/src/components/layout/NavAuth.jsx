// navbar-rechts: warenkorb-icon mit badge und nutzer-dropdown (oder anmelden-button).

import { createSignal, onMount, onCleanup, Show } from "solid-js";
import { getUserInfo, isLoggedIn, logout } from "../../lib/auth.js";
import { getCartCount, onCartChange } from "../../lib/cart.js";

export default function NavAuth() {
  const [user, setUser] = createSignal(null);
  const [loggedIn, setLoggedIn] = createSignal(false);
  const [cartCount, setCartCount] = createSignal(0);
  const [menuOpen, setMenuOpen] = createSignal(false);

  onMount(() => {
    try {
      if (isLoggedIn()) {
        setLoggedIn(true);
        setUser(getUserInfo());
      }
    } catch {
      setLoggedIn(false);
    }

    const updateCartCount = () => setCartCount(getCartCount());
    updateCartCount();
    onCleanup(onCartChange(updateCartCount));

    const onDocClick = (e) => {
      if (!e.target.closest?.("[data-user-menu]")) setMenuOpen(false);
    };
    document.addEventListener("click", onDocClick);
    onCleanup(() => document.removeEventListener("click", onDocClick));
  });

  const handleLogout = () => {
    logout();
    window.location.href = "/";
  };

  const displayName = () => user()?.username ?? user()?.email ?? "Nutzer";
  const initial = () => displayName().charAt(0).toUpperCase();

  return (
    <div class="flex items-center gap-2">
      {/* Warenkorb */}
      <a
        href="/cart"
        class="relative h-9 w-9 flex items-center justify-center text-white hover:bg-white/15 rounded-md transition-colors"
        aria-label="Warenkorb"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
          />
        </svg>
        <Show when={cartCount() > 0}>
          <span class="absolute -top-1 -right-1 bg-[#E8400C] text-white text-[10px] font-black rounded-full min-w-4.5 h-4.5 px-1 flex items-center justify-center border border-[#111111]">
            {cartCount()}
          </span>
        </Show>
      </a>

      {/* Anmelden / Nutzer-Menü */}
      <Show
        when={loggedIn()}
        fallback={
          <a
            href="/login"
            class="h-9 px-4 flex items-center bg-[#E8400C] border-2 border-[#E8400C] rounded-md text-sm font-bold text-white hover:bg-[#C73509] transition-colors"
          >
            Anmelden
          </a>
        }
      >
        <div class="relative" data-user-menu>
          <button
            type="button"
            onClick={() => setMenuOpen(!menuOpen())}
            class="h-9 flex items-center gap-2 bg-white/10 hover:bg-white/20 pl-1.5 pr-3 rounded-md text-white transition-colors border border-white/20"
            aria-haspopup="true"
            aria-expanded={menuOpen()}
            aria-label="Benutzermenü"
          >
            <span class="flex items-center justify-center w-6 h-6 rounded-sm bg-[#E8400C] text-white text-xs font-black">
              {initial()}
            </span>
            <span class="text-sm font-semibold hidden sm:inline max-w-32 truncate">
              {displayName()}
            </span>
            <svg
              class={`w-3.5 h-3.5 text-white/60 transition-transform duration-200 ${menuOpen() ? "rotate-180" : ""}`}
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          <Show when={menuOpen()}>
            <div class="absolute right-0 mt-2 w-52 bg-white border-2 border-[#111111] rounded-md shadow-[4px_4px_0_#111111] py-2 z-50">
              <div class="px-4 pb-2 mb-1 border-b-2 border-[#111111]">
                <p class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Angemeldet als</p>
                <p class="text-sm font-bold text-[#111111] truncate mt-0.5">{displayName()}</p>
              </div>
              <button
                type="button"
                onClick={handleLogout}
                class="block w-full text-left px-4 py-2 text-sm font-bold text-[#E8400C] hover:bg-[#F2EFE8] transition-colors"
              >
                Abmelden
              </button>
            </div>
          </Show>
        </div>
      </Show>
    </div>
  );
}
