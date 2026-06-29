// registrierungsformular. der auth-service erwartet nur email + passwort, keinen benutzernamen.

import { createSignal, Show } from "solid-js";
import { registerUser, errorMessage } from "../../lib/api.js";
import ErrorBanner from "../ui/ErrorBanner.jsx";

export default function RegisterForm() {
  const [username, setUsername] = createSignal("");
  const [email, setEmail] = createSignal("");
  const [firstName, setFirstName] = createSignal("");
  const [lastName, setLastName] = createSignal("");
  const [phoneNumber, setPhoneNumber] = createSignal("");
  const [addressStreet, setAddressStreet] = createSignal("");
  const [addressHouseNumber, setAddressHouseNumber] = createSignal("");
  const [addressPostalCode, setAddressPostalCode] = createSignal("");
  const [addressCity, setAddressCity] = createSignal("");
  const [addressCountry, setAddressCountry] = createSignal("");
  const [password, setPassword] = createSignal("");
  const [confirmPassword, setConfirmPassword] = createSignal("");
  const [error, setError] = createSignal("");
  const [loading, setLoading] = createSignal(false);

  const passwordsMatch = () => password() === confirmPassword();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!username().trim()) {
      setError("Bitte einen Benutzernamen angeben.");
      return;
    }
    if (password().length < 8) {
      setError("Das Passwort muss mindestens 8 Zeichen lang sein.");
      return;
    }
    if (!passwordsMatch()) {
      setError("Die Passwörter stimmen nicht überein.");
      return;
    }
    setLoading(true);
    try {
      await registerUser(username(), email(), password(), firstName(), lastName(), phoneNumber(), {
        street: addressStreet(),
        houseNumber: addressHouseNumber(),
        postalCode: addressPostalCode(),
        city: addressCity(),
        country: addressCountry(),
      });
      window.location.href = "/login?registered=true";
    } catch (err) {
      setError(errorMessage(err, { 400: "Diese E-Mail ist bereits registriert." }));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div class="w-full max-w-md">
      <h2 class="font-black text-[#111111] mb-2 tracking-tight" style="font-size: 2.5rem; line-height:1;">
        Registrieren
      </h2>
      <p class="text-[#666] text-sm mb-8">Schon ein Konto?{" "}
        <a href="/login" class="font-bold text-[#E8400C] hover:underline">Anmelden</a>
      </p>

      <form class="space-y-5" onSubmit={handleSubmit}>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="username">Benutzername</label>
          <input
            id="username" type="text" required
            class="input-field"
            value={username()}
            onInput={(e) => setUsername(e.target.value)}
          />
        </div>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="email">E-Mail</label>
          <input
            id="email" type="email" required
            class="input-field"
            value={email()}
            onInput={(e) => setEmail(e.target.value)}
          />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-bold text-[#111111] mb-1.5" for="firstName">Vorname</label>
            <input
              id="firstName" type="text"
              class="input-field"
              value={firstName()}
              onInput={(e) => setFirstName(e.target.value)}
            />
          </div>
          <div>
            <label class="block text-sm font-bold text-[#111111] mb-1.5" for="lastName">Nachname</label>
            <input
              id="lastName" type="text"
              class="input-field"
              value={lastName()}
              onInput={(e) => setLastName(e.target.value)}
            />
          </div>
        </div>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="phoneNumber">Telefon (optional)</label>
          <input
            id="phoneNumber" type="tel"
            class="input-field"
            value={phoneNumber()}
            onInput={(e) => setPhoneNumber(e.target.value)}
          />
        </div>

        <div class="pt-2">
          <p class="text-xs font-bold text-[#666] uppercase tracking-wider mb-3">Adresse (optional)</p>
          <div class="grid grid-cols-3 gap-3">
            <div class="col-span-2">
              <label class="block text-sm font-bold text-[#111111] mb-1.5" for="addressStreet">Straße</label>
              <input id="addressStreet" type="text" class="input-field" value={addressStreet()} onInput={(e) => setAddressStreet(e.target.value)} />
            </div>
            <div>
              <label class="block text-sm font-bold text-[#111111] mb-1.5" for="addressHouseNumber">Nr.</label>
              <input id="addressHouseNumber" type="text" class="input-field" value={addressHouseNumber()} onInput={(e) => setAddressHouseNumber(e.target.value)} />
            </div>
          </div>
          <div class="grid grid-cols-3 gap-3 mt-3">
            <div>
              <label class="block text-sm font-bold text-[#111111] mb-1.5" for="addressPostalCode">PLZ</label>
              <input id="addressPostalCode" type="text" class="input-field" value={addressPostalCode()} onInput={(e) => setAddressPostalCode(e.target.value)} />
            </div>
            <div>
              <label class="block text-sm font-bold text-[#111111] mb-1.5" for="addressCity">Stadt</label>
              <input id="addressCity" type="text" class="input-field" value={addressCity()} onInput={(e) => setAddressCity(e.target.value)} />
            </div>
            <div>
              <label class="block text-sm font-bold text-[#111111] mb-1.5" for="addressCountry">Land</label>
              <input id="addressCountry" type="text" class="input-field" value={addressCountry()} onInput={(e) => setAddressCountry(e.target.value)} />
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="password">Passwort</label>
          <input
            id="password" type="password" required minLength="8"
            class="input-field"
            value={password()}
            onInput={(e) => setPassword(e.target.value)}
          />
          <p class="text-xs text-[#999] mt-1.5">Mindestens 8 Zeichen.</p>
        </div>
        <div>
          <label class="block text-sm font-bold text-[#111111] mb-1.5" for="confirmPassword">Passwort bestätigen</label>
          <input
            id="confirmPassword" type="password" required minLength="8"
            class="input-field"
            value={confirmPassword()}
            onInput={(e) => setConfirmPassword(e.target.value)}
          />
          <Show when={confirmPassword() && !passwordsMatch()}>
            <p class="text-xs text-[#E8400C] font-semibold mt-1.5">Die Passwörter stimmen nicht überein.</p>
          </Show>
        </div>

        <button type="submit" disabled={loading()} class="btn-primary w-full mt-2">
          {loading() ? "Registrieren..." : "Konto erstellen →"}
        </button>
      </form>

      <Show when={error()}>
        <div class="mt-4 bg-red-50 border-2 border-red-400 rounded-md px-4 py-3 text-sm font-semibold text-red-700">
          {error()}
        </div>
      </Show>
    </div>
  );
}
