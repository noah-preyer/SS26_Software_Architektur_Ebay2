import { createSignal, onMount } from "solid-js";
import { getUserInfo } from "../../lib/auth.js";

export default function UserProfile() {
  const [user, setUser] = createSignal(null);

  onMount(() => {
    setUser(getUserInfo());
  });

  const displayName = () => user()?.username ?? user()?.email ?? "Unbekannt";
  const initial = () => displayName().charAt(0).toUpperCase();

  return (
    <div class="max-w-lg">
      <div class="flex items-center gap-5 pb-6 mb-6 border-b-2 border-[#111111]">
        <span class="flex items-center justify-center w-16 h-16 rounded-full bg-[#E8400C] text-white text-2xl font-black">
          {initial()}
        </span>
        <div>
          <p class="text-xl font-bold">{displayName()}</p>
          <p class="text-sm text-[#666666]">{user()?.email ?? ""}</p>
        </div>
      </div>

      <dl class="space-y-4">
        <div>
          <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Benutzername</dt>
          <dd class="text-sm font-semibold mt-0.5">{user()?.username ?? "—"}</dd>
        </div>
        <div>
          <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">E-Mail</dt>
          <dd class="text-sm font-semibold mt-0.5">{user()?.email ?? "—"}</dd>
        </div>
        <div>
          <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Benutzer-ID</dt>
          <dd class="text-sm font-semibold mt-0.5 font-mono">{user()?.id ?? "—"}</dd>
        </div>
      </dl>
    </div>
  );
}
