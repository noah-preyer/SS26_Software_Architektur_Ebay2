LetzterPreis. (ebay2) (Frontend)

Frontend für einen kleinen ebay kopie. astroJS + solidSJ + tailwind v4.

Das echte backend mit spring boot microservices hinter einem api-gateway ist noch nicht eingabaut. Deswegen wurde ein dummy server zum testen, mit den foldernamen `test-backend/`erstellt der diese schnittstelle in etwa nachbaut.

00. Starten

Wenn .env datei nicht vorhanden einen .env datei mit dem folgenden Inhalt erstellen:

BACKEND_TARGET=http://localhost:8090
PUBLIC_USE_MOCK=false

----

Danach braucht es nur node mit zwei terminals oder alternative direkt per Docker, siehe etwas weiter unten:

```bash
npm install
npm run backend   # test-backend auf :8090
npm run dev       # frontend auf :4321
```

dann http://localhost:4321 öffnen.

Alternativ alles zusammen mit:

`docker compose up`.

----

Fürs einloggen gibts fünf demo-nutzer ohne festes passwort. e-mail + irgendein passwort
ab 8 zeichen reichen aus: `max@example.com`, `anna@example.com`, `tom@example.com`, `lisa@example.com`, `demo@example.com`.

01. Konfiguration

Die konfiguration steht in der `.env` im projekt-wurzelordner. falls sie fehlt (z.b. nach
einem frischen clone), einfach selbst anlegen mit:

```bash
BACKEND_TARGET=http://localhost:8090
PUBLIC_USE_MOCK=false
```

`BACKEND_TARGET` sagt, wo das backend liegt (aktuell dummy backend `:8090`). alle
browser-requests gehen an `/api`, der dev-server leitet das an `BACKEND_TARGET` weiter –
dadurch kein cors. fürs echte backend einfach auf `:8080` umstellen.

02. Was aktuell mit dem Frontend testbar funktioniert.

- ohne login stöbern und ein Produkt ins Warenkorb hinzufügen (übersicht + detailseite)
- registrieren / login
- inserat anlegen mit bild (wird im browser auf 4:3 zugeschnitten + komprimiert, als
  data-url verschickt, kein upload-endpunkt nötig)
- eigene anzeigen bearbeiten / löschen
- warenkorb, kasse, „meine käufe"
- doppelkauf -> Fehler hinweis
- wenn das backend off ist, zeigen die lese-seiten beispieldaten + banner

noch nicht implementiert: bestellungen/zahlungen werden nur simuliert.

03. test-backend

Ein einzelner node-server (`test-backend/`), keine abhängigkeiten, läuft auf :8090 und
speichert in `data.json`. bildet die gateway-routen `/auth`, `/products`, `/users` nach.
`npm run reset` setzt die daten zurück.

04. Endpunkte (was das echte backend liefern muss)

geschützte calls bekommen `Authorization: Bearer <token>`. der `sub` im jwt muss die
numerische user-id sein.

| methode | pfad | token | antwort |
|---|---|---|---|
| POST | `/auth/register` | – | `201 {id, email}`, doppelte mail → 400 |
| POST | `/auth/login` | – | `200` token + user, falsch → 400 |
| GET | `/products` | – | liste, `?category=` filtert |
| GET | `/products/{id}` | – | produkt oder 404 |
| POST | `/products` | wird benötigt | anlegen, `sellerId` aus dem token |
| PUT | `/products/{id}` | wird benötigt | bearbeiten (nur eigene) |
| DELETE | `/products/{id}` | wird benötigt | löschen → 204 |
| POST | `/products/{id}/buy` | wird benötigt | kaufen → 200 oder 409 |
| GET | `/users/{id}` | – | `{id, username, email}` oder 404 |

`GET /products` muss ohne token gehen (gäste), und login/register-fehler müssen 400 sein

datenformate:

```jsonc
// produkt
{
  "id": 1,
  "title": "iPhone 15 Pro",
  "description": "…",            // oder null
  "price": 950,                  // > 0
  "category": "Elektronik",      // oder null
  "sellerId": 1,
  "status": "AVAILABLE",         // "AVAILABLE" | "SOLD"
  "imageUrls": ["data:image/…"], // string[]
  "createdAt": "2026-06-11T12:34:56.000Z"
}

// login-antwort
{
  "access_token": "<JWT>",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": { "id": 1, "username": "max_mueller", "email": "max@example.com" }
}
```

kategorien sind aktuell fest: Elektronik, Kleidung, Möbel, Sport, Fahrzeuge.

05. auth

token + user liegen getrennt im sessionStorage (`src/lib/auth.js`), nur fürs tab, nicht
persistent. signatur prüft das backend, das frontend liest nur den inhalt; abgelaufen
merkt es am 401. den login-status erst in `onMount` lesen, nicht beim SSR – sonst
hydration-mismatch (siehe `ProductDetail.jsx`, `NavAuth.jsx`).
