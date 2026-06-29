// bestellstatus-badge für "Meine Käufe": pollt order_service, bis ein endzustand erreicht ist.
// schlägt der call fehl (z.b. kein order_service erreichbar), bleibt das badge einfach leer.

import { createSignal, onCleanup, onMount, Show } from "solid-js";
import { getOrder } from "../../lib/api.js";

const STATUS_LABELS = {
  CREATED: "Bestellt",
  PAID: "Bezahlt",
  SHIPPED: "Versendet",
  DELIVERED: "Geliefert",
  CANCELLED: "Storniert",
  REFUNDED: "Erstattet",
};

const STATUS_STYLES = {
  CREATED: "bg-white text-[#111111] border-[#111111]",
  PAID: "bg-[#FFF4E0] text-[#9A5B00] border-[#9A5B00]",
  SHIPPED: "bg-[#E6F0FF] text-[#0B4FA0] border-[#0B4FA0]",
  DELIVERED: "bg-[#111111] text-white border-[#111111]",
  CANCELLED: "bg-red-50 text-red-700 border-red-700",
  REFUNDED: "bg-red-50 text-red-700 border-red-700",
};

const TERMINAL_STATUSES = new Set(["DELIVERED", "CANCELLED", "REFUNDED"]);
const POLL_INTERVAL_MS = 3000;

export default function OrderStatusBadge(props) {
  const [status, setStatus] = createSignal(null);
  let timer;

  const poll = async () => {
    try {
      const order = await getOrder(props.orderId);
      setStatus(order?.status ?? null);
      if (order?.status && !TERMINAL_STATUSES.has(order.status)) {
        timer = setTimeout(poll, POLL_INTERVAL_MS);
      }
    } catch {
      // kein order_service erreichbar - badge bleibt einfach aus.
    }
  };

  onMount(() => {
    if (props.orderId) poll();
  });

  onCleanup(() => clearTimeout(timer));

  return (
    <Show when={status()}>
      <span class={`shrink-0 text-xs font-black px-2.5 py-1 rounded-sm uppercase tracking-wider border-2 ${STATUS_STYLES[status()] ?? STATUS_STYLES.CREATED}`}>
        {STATUS_LABELS[status()] ?? status()}
      </span>
    </Show>
  );
}
