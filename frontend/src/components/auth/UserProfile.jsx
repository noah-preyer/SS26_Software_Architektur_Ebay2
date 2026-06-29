import { createSignal, onMount, Show, For } from "solid-js";
import { getUserInfo, authHeader } from "../../lib/auth.js";
import { apiFetch } from "../../lib/api.js";

export default function UserProfile() {
  const [user, setUser] = createSignal(null);
  const [addresses, setAddresses] = createSignal([]);
  const [loading, setLoading] = createSignal(true);

  onMount(async () => {
    const session = getUserInfo();
    if (!session?.id) {
      setLoading(false);
      return;
    }
    try {
      const headers = { ...authHeader() };
      const userData = await apiFetch(`/user/by-auth/${session.id}`, { headers });
      setUser(userData);
      try {
        const addressData = await apiFetch(`/user/${userData.id}/addresses`, { headers });
        setAddresses(addressData ?? []);
      } catch {
        setAddresses([]);
      }
    } catch {
      setUser(session);
    } finally {
      setLoading(false);
    }
  });

  const displayName = () => user()?.username ?? user()?.email ?? "Unbekannt";
  const initial = () => displayName().charAt(0).toUpperCase();

  const addressLabel = (a) =>
    `${a.street} ${a.houseNumber}, ${a.postalCode} ${a.city}, ${a.country}`;

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

      <Show when={!loading()} fallback={<p class="text-sm text-[#666]">Lade Profil...</p>}>
        <dl class="space-y-4 mb-8">
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Benutzername</dt>
            <dd class="text-sm font-semibold mt-0.5">{user()?.username ?? "—"}</dd>
          </div>
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">E-Mail</dt>
            <dd class="text-sm font-semibold mt-0.5">{user()?.email ?? "—"}</dd>
          </div>
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Vorname</dt>
            <dd class="text-sm font-semibold mt-0.5">{user()?.firstName ?? "—"}</dd>
          </div>
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Nachname</dt>
            <dd class="text-sm font-semibold mt-0.5">{user()?.lastName ?? "—"}</dd>
          </div>
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Telefon</dt>
            <dd class="text-sm font-semibold mt-0.5">{user()?.phoneNumber ?? "—"}</dd>
          </div>
          <div>
            <dt class="text-[10px] font-bold text-[#666] uppercase tracking-wider">Benutzer-ID</dt>
            <dd class="text-sm font-semibold mt-0.5 font-mono">{user()?.id ?? "—"}</dd>
          </div>
        </dl>

        <h2 class="text-lg font-bold mb-3 border-b-2 border-[#111111] pb-2">Adressen</h2>
        <Show when={addresses().length > 0} fallback={<p class="text-sm text-[#666]">Keine Adressen hinterlegt.</p>}>
          <For each={addresses()}>
            {(addr) => (
              <div class="bg-white border-2 border-[#111111] rounded-md p-4 mb-3">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-[10px] font-bold uppercase tracking-wider text-[#E8400C]">
                    {addr.addressTypeDisplayName}
                  </span>
                  <Show when={addr.defaultAddress}>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-[#666]">(Standard)</span>
                  </Show>
                </div>
                <p class="text-sm font-semibold">{addr.street} {addr.houseNumber}</p>
                <p class="text-sm text-[#666]">{addr.postalCode} {addr.city}</p>
                <p class="text-sm text-[#666]">{addr.country}</p>
              </div>
            )}
          </For>
        </Show>
      </Show>
    </div>
  );
}
