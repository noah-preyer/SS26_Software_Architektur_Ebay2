// seiten-schutz: nicht eingeloggte nutzer werden zu /login weitergeleitet.

import { createSignal, onMount, Show } from "solid-js";
import { isLoggedIn } from "../../lib/auth.js";

export default function AuthGuard(props) {
  const [allowed, setAllowed] = createSignal(false);

  onMount(() => {
    if (isLoggedIn()) {
      setAllowed(true);
    } else {
      window.location.href = "/login";
    }
  });

  return <Show when={allowed()}>{props.children}</Show>;
}
