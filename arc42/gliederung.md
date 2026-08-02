# Gliederung: Architekturdokumentation „eBay-Klon als Microservices-Architektur"

> Angelehnt an das arc42-Template (https://arc42.de/), Zielumfang 5–10 Seiten.
> Alle Diagramme werden mit Draw.io erstellt und als `.drawio`-Dateien im Repo abgelegt.
> Es wird nur dokumentiert, was im Kontext des Moduls Softwarearchitektur sinnvoll ist —
> arc42-Kapitel, die dafür wenig beitragen, werden bewusst gekürzt oder zusammengelegt.

---

## 1. Einführung und Ziele  *(ca. 0,5–1 Seite)*

- 1.1 Aufgabenstellung: Demoprojekt „eBay-Klon" im Modul Softwarearchitektur;
  gewählter Architekturstil: **Microservices** (vorrangig nach M. Richards,
  „Handbuch moderner Softwarearchitektur", + weitere Literatur)
- 1.2 Fachliche Kernfunktionen: Login/Registrierung, Produkte einstellen /
  bearbeiten / löschen, Produkte kaufen (Bestellung + E-Mail-Bestätigung)
- 1.3 Qualitätsziele (Top 3–5): z. B. Modularität/unabhängige Deploybarkeit,
  lose Kopplung, Skalierbarkeit einzelner Services, Sicherheit (zentrale
  JWT-Prüfung), Erweiterbarkeit
- 1.4 Stakeholder (kurz): Dozent, Projektgruppe

## 2. Randbedingungen  *(ca. 0,25–0,5 Seite)*

- Organisatorisch: Gruppenarbeit, Zwischenbesprechung mit Dozent, 5–10 Seiten Doku
- Technisch: freie Tech-Stack-Wahl → polyglotter Stack als bewusste Demonstration
  der Microservices-Eigenschaft (Java/Spring Boot, Python/Flask, Astro-Frontend),
  Betrieb via Docker Compose, Diagramme via Draw.io

## 3. Kontextabgrenzung  *(ca. 0,5 Seite)*

- 3.1 Fachlicher Kontext: Nutzer (Käufer/Verkäufer) ↔ System; E-Mail-Versand als
  externe Schnittstelle (SMTP)
- 3.2 Technischer Kontext: Browser → Frontend → API Gateway → Services
- **Diagramm (Draw.io): Kontextdiagramm** (System als Blackbox mit Nachbarsystemen)

## 4. Lösungsstrategie  *(ca. 0,5–1 Seite)*

- Warum Microservices für diesen Anwendungsfall
- Zentrale Entscheidungen im Überblick:
  - Fachlicher Service-Schnitt (Auth, User, Product, Order, Email)
  - API Gateway (Spring Cloud Gateway) als einziger Einstiegspunkt
    (Routing + zentrale JWT-Validierung)
  - Database-per-Service (jeder Service eigene PostgreSQL-Instanz)
  - Synchron REST zwischen Services, asynchron MQTT (Publish/Subscribe)
    für E-Mail-Benachrichtigungen
  - Horizontale Skalierung exemplarisch: Product Service mit 3 Replikaten
    hinter Nginx-Load-Balancer
  - Polyglotte Implementierung (Java/Spring Boot + Python/Flask) als Beleg
    für Technologie-Freiheit pro Service

## 5. Bausteinsicht  *(ca. 1,5–2 Seiten — Kernkapitel)*

- 5.1 Gesamtsystem (Ebene 1): API Gateway, Auth-, User-, Product- (3 Replikate
  + Nginx-Proxy), Order-, Email-Service, Frontend (Astro), MQTT-Broker
  (Mosquitto), je Service eigene PostgreSQL-Datenbank
- 5.2 Kurzbeschreibung jedes Service als Blackbox (Verantwortung, Schnittstelle,
  Tech-Stack, eigene DB) — tabellarisch, um Platz zu sparen
- **Diagramm (Draw.io): Baustein-/Übersichtsdiagramm** aller Services mit
  Datenbanken, Load Balancer und Broker
- Optional 5.3: Ein Service exemplarisch als Whitebox (z. B. Order Service:
  Controller → Service → Repository, Clients zu Product/User/Email)

## 6. Laufzeitsicht  *(ca. 1 Seite)*

- 6.1 Szenario „Login": Frontend → Gateway → Auth Service → JWT (HS256, 60 min)
- 6.2 Szenario „Produkte kaufen" (tatsächlicher Ablauf laut Code):
  Frontend → Gateway (JWT-Prüfung, `X-User-Id`) → **Product Service**
  (`POST /products/bulk-buy`, reserviert alle Artikel per `markAllAsSold`) →
  Order Service (`POST /order`, holt je Artikel Titel/Preis zurück beim
  Product Service) → `PUT /order/{id}/paid` → Order Service holt E-Mail beim
  User Service und publiziert MQTT-Event `order/complete` →
  Email Service (Subscriber) rendert Template und versendet per SMTP
- Wichtig für die Doku: Das MQTT-Event entsteht erst beim Statuswechsel auf
  PAID, nicht beim Anlegen der Bestellung. Bei Fehlern setzt der Product Service
  die Artikel per `markAllAsAvailable` zurück → **kompensierende Transaktion**
- **Diagramm (Draw.io): 1–2 Sequenzdiagramme** (mind. der Kauf-/Bestellfluss,
  da er synchrone UND asynchrone Kommunikation zeigt)

## 7. Verteilungssicht  *(ca. 0,5 Seite)*

- Docker Compose (modular: eine Compose-Datei pro Service, per `include`
  zusammengeführt): ein Container pro Service + DB-Container + Mosquitto-Broker,
  Product Service skaliert auf 3 Replikate hinter Nginx
- **Diagramm (Draw.io): Deployment-Diagramm** (Container, Ports, Netzwerk,
  Replikate/Load Balancer)

## 8. Querschnittliche Konzepte  *(ca. 0,5–1 Seite)*

- Sicherheit: JWT-Ausstellung im Auth Service, zentrale Validierung im Gateway
  (GlobalFilter), öffentliche Endpunkte (Login/Registrierung, Produkt-Browsing)
  vs. geschützte Endpunkte, Identitätsweitergabe via `X-User-Id`-Header
- Kommunikation: REST (synchron, Service-zu-Service) vs. MQTT/Publish-Subscribe
  (asynchron, `order/complete`) — Kriterien für die Wahl
- Skalierung: zustandslose Services, Replikation + Load Balancing (Nginx)
- Persistenz: Database-per-Service, Konsequenzen (keine Joins über Services,
  Referenzen über IDs statt Fremdschlüssel)

## 9. Architekturentscheidungen (ADRs)  *(ca. 0,5–1 Seite)*

Kurzform (Kontext → Entscheidung → Konsequenz), z. B.:
- ADR-1: Microservices statt (modularer) Monolith
- ADR-2: Zentrales API Gateway mit JWT-Validierung
- ADR-3: MQTT für E-Mail-Benachrichtigungen statt synchronem REST-Aufruf
- ADR-4: Database-per-Service
- ADR-5: Polyglotter Tech-Stack (Java + Python)
- ADR-6: Replikation des Product Service mit Nginx als Load Balancer
- ADR-7: Kompensierende Transaktion statt verteilter Transaktion beim Checkout

## 10. Qualitätsanforderungen  *(ca. 0,5 Seite)*

- Qualitätsbaum kompakt oder 3–4 konkrete Qualitätsszenarien, z. B.:
  „Email-Service fällt aus → Bestellungen funktionieren weiter (lose Kopplung
  durch MQTT)", „Lastspitze beim Produkt-Browsing → 3 Replikate teilen sich
  die Last", „neuer Service kann unabhängig deployt werden"

## 11. Risiken und technische Schulden  *(ca. 0,25–0,5 Seite)*

- z. B.: kein zentrales Logging/Monitoring (keine Correlation-ID über Services),
  keine Service Discovery, Gateway als Single Point of Failure,
  zyklische Abhängigkeit Product Service ↔ Order Service,
  Payment-/Image-Service aktuell deaktiviert (old-services),
  Secrets (JWT-Secret, SMTP-Zugangsdaten) im Klartext in Compose-Dateien

## 12. Fazit: Bewertung des Architekturstils  *(ca. 0,5 Seite)*

- Vorteile / Nachteile von Microservices anhand der eigenen Erfahrungen im Projekt
- Kriterien: Wann ist der Stil sinnvoll, wann nicht? (Bezug zu Richards,
  Vorbereitung auf die Abschlusspräsentation)

- Glossar nur bei Bedarf (arc42 Kap. 12) — sonst weglassen
- Literaturverzeichnis (Richards + weitere Quellen aus der Literaturrecherche)

---

## Draw.io-Diagramme (Übersicht)

Alle Diagramme liegen als `.drawio`-Dateien im Unterordner `diagramme/` und lassen
sich unter https://app.diagrams.net/ oder mit der Draw.io-Desktop-App öffnen und
bearbeiten.

| Datei | Diagramm | Kapitel | Abb. im Word-Dokument |
|-------|----------|---------|-----------------------|
| `diagramme/01-kontextdiagramm.drawio` | Kontextdiagramm | 3 | Abbildung 1 |
| `diagramme/02-bausteinsicht.drawio` | Bausteinsicht mit DBs, Load Balancer und MQTT-Broker | 5.1 | Abbildung 2 |
| `diagramme/06-whitebox-order-service.drawio` | Whitebox Order Service | 5.2 | Abbildung 3 |
| `diagramme/04-sequenz-login.drawio` | Sequenzdiagramm „Login/JWT" | 6.1 | Abbildung 4 |
| `diagramme/03-sequenz-produkt-kaufen.drawio` | Sequenzdiagramm „Produkte kaufen" inkl. Kompensation | 6.2 | Abbildung 5 |
| `diagramme/05-deployment.drawio` | Deployment-Diagramm (Docker Compose) | 7 | Abbildung 6 |

---

## Ausgearbeitete Fassung

Der ausformulierte Text liegt als Word-Datei vor: `Architekturdokumentation.docx`
(Titelblatt, automatisches Inhaltsverzeichnis, 12 Kapitel, Literaturverzeichnis).
Die sechs Abbildungen sind dort als beschriftete Platzhalter hinterlegt — aus
Draw.io als PNG exportieren und in den jeweiligen Rahmen einfügen.
