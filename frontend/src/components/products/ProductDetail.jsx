// produktdetailseite: lädt produkt und verkäufer getrennt voneinander.
// 404 schlägt immer durch. wenn GET /users/{id} fehlt, zeigt die seite einfach "unbekannt".

import { createSignal, createResource, createMemo, onMount, Show } from "solid-js";
import { getProduct, getSeller, deleteProduct, errorMessage } from "../../lib/api.js";
import { getUserInfo } from "../../lib/auth.js";
import { addToCart, isInCart, removeFromCart } from "../../lib/cart.js";
import { formatPrice, formatDate } from "../../lib/format.js";
import ImageGallery from "../media/ImageGallery.jsx";
import ErrorBanner from "../ui/ErrorBanner.jsx";
import ConfirmDialog from "../ui/ConfirmDialog.jsx";

export default function ProductDetail(props) {
  const id = props.id;

  const [result, { refetch }] = createResource(() => id, getProduct);

  onMount(() => {
    setCurrentUser(getUserInfo());
    setInCart(isInCart(id));
  });

  const product = () => (result.error ? undefined : result()?.data);

  const [seller] = createResource(
    () => product()?.sellerId,
    (sellerId) => getSeller(sellerId)
  );

  const [inCart, setInCart] = createSignal(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = createSignal(false);
  const [deleteError, setDeleteError] = createSignal("");
  const notFound = () => !result.loading && !result.error && !product();
  const is404 = () => result.error?.status === 404;

  const [currentUser, setCurrentUser] = createSignal(null);

  const isOwner = createMemo(() => {
    const p = product();
    const user = currentUser();
    return !!p && !!user && user.id != null && Number(user.id) === Number(p.sellerId);
  });

  const canAddToCart = createMemo(() => {
    const p = product();
    return !!p && p.status === "AVAILABLE" && !isOwner();
  });

  const toggleCart = () => {
    if (inCart()) {
      removeFromCart(id);
      setInCart(false);
    } else {
      addToCart(id);
      setInCart(true);
    }
  };

  const handleDelete = async () => {
    setDeleteError("");
    try {
      await deleteProduct(id);
      window.location.href = "/my-listings";
    } catch (err) {
      setDeleteError(errorMessage(err));
      setShowDeleteConfirm(false);
    }
  };

  return (
    <div class="py-8">
      {/* Back link */}
      <a href="/" class="inline-flex items-center gap-1.5 text-sm font-bold text-[#666] hover:text-[#111111] transition-colors mb-8 group">
        <svg class="w-4 h-4 transition-transform group-hover:-translate-x-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7"/>
        </svg>
        Zurück zur Übersicht
      </a>

      <Show when={result.loading}>
        <div class="space-y-4 animate-pulse">
          <div class="h-8 bg-[#E0DDD6] rounded-md w-2/3"/>
          <div class="aspect-4/3 bg-[#E0DDD6] rounded-md"/>
        </div>
      </Show>

      <Show when={is404() || notFound()}>
        <div class="card p-8 text-center">
          <p class="text-4xl font-black text-[#E8400C] mb-2">404</p>
          <p class="font-bold text-[#111111]">Produkt nicht gefunden.</p>
        </div>
      </Show>

      <Show when={result.error && !is404()}>
        <ErrorBanner type="error" message={errorMessage(result.error)} />
      </Show>

      <Show when={product()}>
        <Show when={result()?.isMock}>
          <ErrorBanner type="warning" message="Beispieldaten werden angezeigt – Backend nicht erreichbar." />
        </Show>

        {/* 2-Spalten-Layout auf md+: Bild (3/5) links, Info (2/5) rechts */}
        <div class="md:grid md:grid-cols-5 md:gap-10 md:items-start">

          {/* Linke Spalte: Bildgalerie */}
          <div class="md:col-span-3 mb-8 md:mb-0">
            <ImageGallery images={product().imageUrls} />
          </div>

          {/* Rechte Spalte: Header + Preis + CTA */}
          <div class="md:col-span-2">
            <header class="mb-6">
              <div class="flex flex-wrap items-center gap-2 mb-3">
                <Show when={product().category}>
                  <span class="text-xs font-black px-3 py-1 bg-white border-2 border-[#111111] rounded-sm uppercase tracking-wider">
                    {product().category}
                  </span>
                </Show>
                <span class={`text-xs font-black px-3 py-1 rounded-sm uppercase tracking-wider border-2 ${
                  product().status === "SOLD"
                    ? "bg-[#111111] text-white border-[#111111]"
                    : "bg-[#E8400C] text-white border-[#E8400C]"
                }`}>
                  {product().status === "SOLD" ? "Verkauft" : "Verfügbar"}
                </span>
              </div>

              <h1 class="text-3xl md:text-4xl font-black text-[#111111] tracking-tight leading-tight mb-3">
                {product().title}
              </h1>

              <div class="flex items-center gap-2 text-sm text-[#666]">
                <div class="w-7 h-7 rounded-sm bg-[#E8400C] border-2 border-[#111111] flex items-center justify-center text-white font-black text-xs shrink-0">
                  {(seller.loading ? "?" : (seller()?.username ?? "?")).charAt(0).toUpperCase()}
                </div>
                <span>
                  <span class="font-bold text-[#111111]">{seller.loading ? "..." : (seller()?.username ?? "Unbekannt")}</span>
                  {" "}· {formatDate(product().createdAt)}
                </span>
              </div>
            </header>

            {/* Preis + Aktions-CTA */}
            <div class="border-t-2 border-[#111111] pt-6">
              <Show when={deleteError()}>
                <ErrorBanner type="error" message={deleteError()} dismissible />
              </Show>

              <p class="text-xs font-black text-[#999] uppercase tracking-widest mb-1">Preis</p>
              <p class="font-black text-[#111111] tracking-tight mb-6" style="font-size: 2.5rem; line-height:1;">
                {formatPrice(product().price)}
              </p>

              <div class="flex flex-wrap items-center gap-3">
                <Show when={canAddToCart()}>
                  <button
                    type="button"
                    onClick={toggleCart}
                    class={inCart() ? "btn-secondary" : "btn-primary"}
                  >
                    {inCart() ? "← Aus Warenkorb entfernen" : "In den Warenkorb →"}
                  </button>
                </Show>

                <Show when={isOwner()}>
                  <a href={`/my-listings?edit=${product().id}`} class="btn-secondary">Bearbeiten</a>
                  <button type="button" onClick={() => setShowDeleteConfirm(true)} class="btn-secondary" style="border-color:#E8400C; color:#E8400C;">
                    Löschen
                  </button>
                </Show>

                <Show when={!canAddToCart() && !isOwner() && product().status === "SOLD"}>
                  <span class="text-sm font-bold text-[#999] bg-[#F2EFE8] border-2 border-[#ddd] px-4 py-2 rounded-sm">
                    Nicht mehr verfügbar
                  </span>
                </Show>
              </div>
            </div>
          </div>
        </div>

        {/* Beschreibung – vollbreit unter dem Grid */}
        <Show when={product().description}>
          <section class="mt-8 p-6 bg-white border-2 border-[#111111] rounded-md shadow-[3px_3px_0_#111111]">
            <h2 class="text-sm font-black text-[#111111] uppercase tracking-widest mb-3">Beschreibung</h2>
            <p class="text-[#333] leading-relaxed whitespace-pre-line">{product().description}</p>
          </section>
        </Show>
      </Show>

      <ConfirmDialog
        open={showDeleteConfirm()}
        message="Diese Anzeige wirklich löschen?"
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </div>
  );
}
