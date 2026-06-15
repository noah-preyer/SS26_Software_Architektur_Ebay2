// bestätigungsdialog als ersatz für den browser-confirm.

import { Show } from "solid-js";

export default function ConfirmDialog(props) {
  return (
    <Show when={props.open}>
      <div class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="card p-6 w-full max-w-sm">
          <p class="mb-6">{props.message}</p>
          <div class="flex justify-end gap-3">
            <button type="button" onClick={props.onCancel} class="btn-secondary">
              Abbrechen
            </button>
            <button type="button" onClick={props.onConfirm} class="btn-primary">
              {props.confirmLabel ?? "Löschen"}
            </button>
          </div>
        </div>
      </div>
    </Show>
  );
}
