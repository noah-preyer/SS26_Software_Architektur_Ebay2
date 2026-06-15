// filter- und sortierlogik für produktlisten, von mehreren seiten genutzt.

export const PRODUCT_SORT_OPTIONS = [
  { value: "newest", label: "Neueste zuerst" },
  { value: "oldest", label: "Älteste zuerst" },
  { value: "price-asc", label: "Preis aufsteigend" },
  { value: "price-desc", label: "Preis absteigend" },
];

export function filterProducts(list, term) {
  const q = (term ?? "").trim().toLowerCase();
  if (!q) return list;
  return list.filter(
    (p) =>
      p.title?.toLowerCase().includes(q) ||
      p.category?.toLowerCase().includes(q)
  );
}

export function sortProducts(list, sortValue) {
  const copy = [...list];
  switch (sortValue) {
    case "oldest":
      return copy.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    case "price-asc":
      return copy.sort((a, b) => Number(a.price) - Number(b.price));
    case "price-desc":
      return copy.sort((a, b) => Number(b.price) - Number(a.price));
    case "newest":
    default:
      return copy.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  }
}
