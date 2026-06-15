// Formatierung des preises und datums

export function formatPrice(price) {
  return new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(price);
}

export function formatDate(isoString) {
  return new Date(isoString).toLocaleDateString("de-DE");
}
