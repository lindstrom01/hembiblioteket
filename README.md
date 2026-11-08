# Hembibliotek

Skanna ISBN-streckkoder med kameran, hämta bokdata från Libris och spara i en databas.

- `backend/` – Quarkus 3 (Java 25), Hibernate Panache, Flyway, PostgreSQL, REST-klient mot Libris (xsearch) med retry/timeout/cache.
- `frontend/` – Angular (standalone, signals), kameraskanning med `@zxing/browser`.

## Köra lokalt

```powershell
docker compose up -d                 # PostgreSQL
cd backend;  ./mvnw quarkus:dev      # http://localhost:8080  (OpenAPI: /q/openapi)
cd frontend; npm install; npm start  # http://localhost:4200  (proxar /api till 8080)
```

Kameran kräver HTTPS eller `localhost`. För test från mobilen: `ng serve --host 0.0.0.0 --ssl`
(eller en tunnel/reverse proxy med HTTPS). Ange annars ISBN manuellt.

## API

| Metod | Sökväg | Beskrivning |
|---|---|---|
| GET | `/api/lookup/{isbn}` | Förhandsvisa bok från Libris (400 ogiltigt ISBN, 404 ej funnen, 502 Libris nere) |
| GET | `/api/books?q=&genre=&page=&size=` | Sök/lista (sidindelat) |
| GET | `/api/books/genres` | Alla genrer i biblioteket |
| GET/PUT/DELETE | `/api/books/{id}` | Hämta/uppdatera/ta bort |
| POST | `/api/books` | Spara bok (409 om ISBN redan finns) |

## Tester

`cd backend; ./mvnw test` (H2, kräver inte Docker) · `cd frontend; npm test`

Konfiguration via miljövariabler: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGINS`.
