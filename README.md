# E-Commerce Platform (Microservices)

Eine E-Commerce-Plattform als Beispielanwendung für eine Microservice-Architektur.
Sie besteht aus mehreren unabhängig deploybaren Services (Auth, User, Product, Order, Email),
die über ein zentrales API Gateway erreichbar sind.

![Plattform](arc42/images/front_page.png)

## Starten

Voraussetzung: Docker und Docker Compose.

```bash
docker compose up --build
```

Anschließend ist die Plattform im Browser erreichbar:

- Frontend: http://localhost:4321

Beenden mit:

```bash
docker compose down
```

Container, Netzwerke und Volumes (Datenbanken) vollständig zurücksetzen:

```bash
docker compose down -v
```
