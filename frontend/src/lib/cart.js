// warenkorb und kaufhistorie - aktuell alles im sessionstorage, kein backend nötig.

import { buyProduct, getProduct } from "./api.js";

const CART_KEY = "lp_cart";
const PURCHASES_KEY = "lp_purchases";
const ORDER_IDS_KEY = "lp_order_ids";
const CART_EVENT = "lp-cart-updated";

function hasStorage() {
  return typeof window !== "undefined" && !!window.sessionStorage;
}

function readIds(key) {
  if (!hasStorage()) return [];
  try {
    const raw = sessionStorage.getItem(key);
    const ids = raw ? JSON.parse(raw) : [];
    return Array.isArray(ids) ? ids.map(Number) : [];
  } catch {
    return [];
  }
}

function writeIds(key, ids) {
  if (!hasStorage()) return;
  sessionStorage.setItem(key, JSON.stringify(ids));
  if (key === CART_KEY) window.dispatchEvent(new Event(CART_EVENT));
}

export function getCart() {
  return readIds(CART_KEY);
}

export function isInCart(id) {
  return getCart().includes(Number(id));
}

export function addToCart(id) {
  const ids = getCart();
  const numId = Number(id);
  if (!ids.includes(numId)) writeIds(CART_KEY, [...ids, numId]);
}

export function removeFromCart(id) {
  const numId = Number(id);
  writeIds(CART_KEY, getCart().filter((x) => x !== numId));
}

export function clearCart() {
  writeIds(CART_KEY, []);
}

export function getCartCount() {
  return getCart().length;
}

// solidjs-inseln teilen keinen state, daher custom event damit das warenkorb-badge aktuell bleibt.
export function onCartChange(callback) {
  if (typeof window === "undefined") return () => {};
  window.addEventListener(CART_EVENT, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(CART_EVENT, callback);
    window.removeEventListener("storage", callback);
  };
}

export function getPurchases() {
  return readIds(PURCHASES_KEY);
}

function addPurchase(id) {
  const ids = getPurchases();
  const numId = Number(id);
  if (!ids.includes(numId)) {
    sessionStorage.setItem(PURCHASES_KEY, JSON.stringify([...ids, numId]));
  }
}

// produktId -> orderId, nur fürs Order-Status-Badge auf "Meine Käufe".
function readOrderIds() {
  if (!hasStorage()) return {};
  try {
    const raw = sessionStorage.getItem(ORDER_IDS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

function recordOrderId(productId, orderId) {
  if (!hasStorage() || !orderId) return;
  const map = readOrderIds();
  map[Number(productId)] = orderId;
  sessionStorage.setItem(ORDER_IDS_KEY, JSON.stringify(map));
}

export function getOrderId(productId) {
  return readOrderIds()[Number(productId)] ?? null;
}

export async function loadCartItems(ids) {
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

// POST /products/{id}/buy pro artikel. 409 = schon verkauft → aus cart, aber nicht in history.
export async function checkout() {
  const ids = getCart();
  const purchased = [];
  const conflicts = [];
  const errors = [];

  for (const id of ids) {
    try {
      const result = await buyProduct(id);
      addPurchase(id);
      recordOrderId(id, result?.orderId);
      removeFromCart(id);
      purchased.push(id);
    } catch (err) {
      if (err?.status === 409) {
        removeFromCart(id);
        conflicts.push(id);
      } else {
        errors.push({ id, error: err });
      }
    }
  }

  return { purchased, conflicts, errors };
}
