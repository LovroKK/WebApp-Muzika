# BeatSync — pokretanje preko Dockera (za demonstraciju)

Cijela aplikacija (frontend + backend + MySQL baza) pokreće se jednom komandom.
Ne treba ti Live Server, lokalna Java ni lokalni MySQL — sve je u kontejnerima.

## Preduvjeti

- **Docker Desktop** instaliran i **pokrenut** (ikona kita u traci mora biti mirna/zelena).
- Slobodni portovi na računalu: **8081** (frontend), **8080** (backend), **3307** (baza).
  - Ako ti lokalni Spring Boot radi iz IDE-a, **ugasi ga** — drži port 8080.
  - Lokalni MySQL na 3306 NE smeta (Docker baza je na 3307).

## Prvo pokretanje

U root folderu projekta (`WebApp-Muzika`):

```powershell
docker compose up --build
```

Prvi put traje par minuta (gradi backend, preuzima slike). Kad vidiš da backend
ispiše `Started BeatsyncBackendApplication`, sve radi.

Otvori u browseru:

| Komponenta | Adresa |
|------------|--------|
| **Frontend (aplikacija)** | http://localhost:8081 |
| Backend API | http://localhost:8080 |

> Baza se pri prvom pokretanju automatski napuni iz `backend/docker/schema.sql`
> (tvoja izvezena shema + demo podaci).

## Svako sljedeće pokretanje

Kad je već jednom izgrađeno, dovoljno je:

```powershell
docker compose up
```

(bez `--build`, brže — gradi ponovno samo ako si mijenjao kod).

## Zaustavljanje

- U terminalu gdje radi: **Ctrl+C**, pa `docker compose down`.
- Ili iz drugog terminala: `docker compose down`.

Podaci u bazi **ostaju** sačuvani između pokretanja (u Docker volumenu `mysql_data`).

## Stripe plaćanja (TEST mode)

Plaćanje zahtijeva da Stripe može doći do tvog backenda preko webhooka.
Za to služi **Stripe CLI** (`stripe listen`).

1. Instaliraj Stripe CLI: https://docs.stripe.com/stripe-cli (ili `choco install stripe-cli`)
2. Prijavi se jednom: `stripe login`
3. Pokreni tunel prema backendu:
   ```powershell
   stripe listen --forward-to localhost:8080/api/placanja/webhook
   ```
4. CLI ispiše `whsec_...` — **kopiraj ga** u `.env` pod `STRIPE_WEBHOOK_SECRET`.
5. Restartaj backend da pokupi novi secret:
   ```powershell
   docker compose up -d --force-recreate backend
   ```
6. Za testnu karticu koristi broj `4242 4242 4242 4242`, bilo koji budući datum i CVC.

> **Napomena:** webhook secret se mijenja svaki put kad iznova pokreneš `stripe listen`.
> Ako plaćanje "prođe" ali se status ne osvježi, gotovo uvijek je razlog zastarjeli secret.

## Česti problemi

| Problem | Rješenje |
|---------|----------|
| `port is already allocated` (8080) | Ugasi lokalni backend iz IDE-a. |
| `port is already allocated` (8081/3307) | Nešto drugo drži port; promijeni lijevi broj u `docker-compose.yml` (npr. `8082:80`). |
| Backend puca s `Schema-validation` greškom | `schema.sql` se razlikuje od entiteta. Izvezi shemu ponovno (vidi dolje) ili u `.env` privremeno stavi `SPRING_JPA_DDL_AUTO=update`. |
| Baza prazna (0 tablica), backend ne nalazi tablice | `schema.sql` se učitava SAMO pri prvom stvaranju volumena. Ako je volumen ostao od ranije prazan: `docker compose down -v` pa opet `up`. |
| Promijenio sam kod, ali ne vidim promjenu | `docker compose up --build`. |
| Želim čisti restart (obrisati bazu) | `docker compose down -v` pa opet `up` (PAZI: briše sve podatke). |

## Ponovni izvoz baze (ako promijeniš entitete)

Ako dodaš/promijeniš tablice, osvježi `backend/docker/schema.sql` iz lokalne baze:

```powershell
& "E:\MySQLServer\bin\mysqldump.exe" -u root --password=root `
  --databases webapp_muzika --add-drop-table --routines --single-transaction `
  --result-file="backend\docker\schema.sql"
```

Zatim `docker compose down -v` i `docker compose up --build` da se baza ponovno učita.
