// banner für fehler (rot) und hinweise (beige)

import { createSignal, Show } from "solid-js";

function IconError() {
  return (
    <svg class="w-4 h-4 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
      <circle cx="12" cy="12" r="10" />
      <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01" />
    </svg>
  );
}

function IconWarning() {
  return (
    <svg class="w-4 h-4 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
      <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
    </svg>
  );
}

export default function ErrorBanner(props) {
  const [dismissed, setDismissed] = createSignal(false);
  const isError = () => props.type === "error";

  return (
    <Show when={!dismissed()}>
      <div
        class={`border-2 rounded-sm px-4 py-3 mb-4 flex items-start justify-between gap-3 text-sm font-bold ${
          isError()
            ? "bg-[#E8400C] border-[#111111] text-white shadow-[3px_3px_0_#111111]"
            : "bg-[#F2EFE8] border-[#111111] text-[#111111] shadow-[3px_3px_0_#111111]"
        }`}
      >
        <div class="flex items-start gap-2">
          <Show when={isError()} fallback={<IconWarning />}>
            <IconError />
          </Show>
          <p>{props.message}</p>
        </div>
        <Show when={props.dismissible}>
          <button
            type="button"
            onClick={() => setDismissed(true)}
            aria-label="Schließen"
            class="font-black text-lg leading-none hover:opacity-60 transition-opacity shrink-0"
          >
            ×
          </button>
        </Show>
      </div>
    </Show>
  );
}
