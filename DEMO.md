# BeatSync — za demo  na lokalnoj mreži

Step-by-step za dijeljenje aplikacije na lokalnoj mreži


## Kako radi

Frontend više nema hardkodiran `localhost` — backend adresu računa **iz adrese
kojom si otvorio stranicu** (vidi [frontend/components/config.js](frontend/components/config.js)).
Pa ako kolega otvori `http://192.168.1.50:8081`, njegov browser automatski zove
backend na `http://192.168.1.50:8080`. Ništa ne treba mijenjati po uređaju.

---

## Korak 1 — pronaci IP host uređaja (laptopa) na WiFi-ju

ipconfig

IPv4 adresa koju će koristit svi ostali da otvore app.



## Korak 2 — Otvoriti portove u Windows Firewallu 

PowerShell (run as admin) 

New-NetFirewallRule -DisplayName "BeatSync demo" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080,8081


Zatvaranje porta:

Remove-NetFirewallRule -DisplayName "BeatSync demo"


## Korak 3 — (za demonstriranje plaćanja) namjestit Stripe na IP

Stripe nakon plaćanja preusmjerava korisnika natrag na success/cancel stranicu.
Te adrese su po defaultu `localhost`, ne radi s drugih uređaja. 
U file `.env` zamijenit stripe success/cancel sa IP-em hosta.


STRIPE_SUCCESS_URL=http://192.168.1.50:8081/payment-success.html


U CMD pokrenut Stripe webhook tunel (forwarda na tvoj laptop):

stripe listen --forward-to localhost:8080/api/placanja/webhook


Kopirat `whsec_...` koji ispiše u `.env` pod `STRIPE_WEBHOOK_SECRET`.
(Webhook ostaje na `localhost:8080`)

>Stripe TEST modu -> testna kartica `4242 4242 4242 4242`

## Korak 4 — Pokrenut aplikaciju

U root folderu projekta:

```powershell
docker compose up --build
```

Prvi put obavezno `--build` (jer su se promijenile frontend datoteke).
Sljedeći put dovoljno `docker compose up`. Kad backend ispiše
`Started BeatsyncBackendApplication`, sve radi.

> Ako si u Koraku 3 mijenjao `.env`, osvježi backend:
> ```powershell
> docker compose up -d --force-recreate backend
> ```

## Korak 5 — Svi se spajaju

Svi uređaji na **istom WiFi** otvore u browseru:


http://<IPv4>:8081





## Provjera da radi

1. S **drugog laptopa** otvori `http://<IP>:8081`, otvori DevTools → Network.
   API pozivi moraju ići na `http://<IP>:8080/api/...` (NE `localhost`) i
   vraćati 200 (ne CORS grešku).
2. Napravi login, pregledaj DJ-eve, otvori chat — sve treba raditi kao na tvom laptopu.
3. (Ako testiraš plaćanje) plati test karticom; u terminalu sa `stripe listen`
   mora se pojaviti event, a stranica se preusmjeriti na `payment-success.html`.

## Česti problemi

P1: Drugi uređaj ne može učitati stranicu | 
Rješenje: Firewall (Korak 2) nije postavljen, ili niste na istoj WiFi mreži. 

P2: Stranica se učita ali ništa se ne dohvaća (CORS/Network greške)
Rješenje: Provjeri da si pokrenuo `docker compose up --build` NAKON izmjena (frontend se mora ponovno izgraditi).

P3: Nakon plaćanja redirect ide na `localhost` i ne radi
Rješenje: `STRIPE_SUCCESS_URL`/`CANCEL_URL` u `.env` nisu postavljeni na IP (Korak 3).

P4: IP se promijenio (drugi WiFi)
Rješenje: Ništa ne treba mijenjati u kodu — samo svima reci novi IP iz `ipconfig`. Stripe URL-ove u `.env` ažuriraj ako koristiš plaćanje.

P5: Mobitel kaže "nije sigurno" / blokira
Rješenje: Normalno za `http://` bez certifikata; za demo se može nastaviti. Kamera/lokacija nije potrebnoi uključiti.

> **Napomena o host mrežama:** neke javne/uredske WiFi mreže imaju
> "client isolation" (uređaji se međusobno ne vide). Ako pristup ne radi unatoč
> ispravnom IP-u i firewallu, mreža vjerojatno izolira klijente — koristi vlastiti
> hotspot s mobitela kao WiFi za demo.
