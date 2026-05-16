# Graph Report - .  (2026-05-16)

## Corpus Check
- Corpus is ~17,086 words - fits in a single context window. You may not need a graph.

## Summary
- 348 nodes · 381 edges · 61 communities (18 shown, 43 thin omitted)
- Extraction: 81% EXTRACTED · 19% INFERRED · 0% AMBIGUOUS · INFERRED: 72 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Frontend Pages & API Endpoints|Frontend Pages & API Endpoints]]
- [[_COMMUNITY_Equipment & Favorites Controllers|Equipment & Favorites Controllers]]
- [[_COMMUNITY_Security & Chat Infrastructure|Security & Chat Infrastructure]]
- [[_COMMUNITY_Auth & User Repositories|Auth & User Repositories]]
- [[_COMMUNITY_Domain Enums & Messaging|Domain Enums & Messaging]]
- [[_COMMUNITY_Response DTOs|Response DTOs]]
- [[_COMMUNITY_Business & DJ Controllers|Business & DJ Controllers]]
- [[_COMMUNITY_Job Offer Booking Flow|Job Offer Booking Flow]]
- [[_COMMUNITY_Equipment Frontend Components|Equipment Frontend Components]]
- [[_COMMUNITY_JWT Security Pipeline|JWT Security Pipeline]]
- [[_COMMUNITY_Reservation Controller|Reservation Controller]]
- [[_COMMUNITY_Navbar Web Component|Navbar Web Component]]
- [[_COMMUNITY_Equipment Local Storage|Equipment Local Storage]]
- [[_COMMUNITY_Chat Web Component|Chat Web Component]]
- [[_COMMUNITY_Spring Boot Backend Config|Spring Boot Backend Config]]
- [[_COMMUNITY_Security Config|Security Config]]
- [[_COMMUNITY_Message Repository|Message Repository]]
- [[_COMMUNITY_Equipment Card Component|Equipment Card Component]]
- [[_COMMUNITY_DJ Controller|DJ Controller]]
- [[_COMMUNITY_Equipment Locations|Equipment Locations]]
- [[_COMMUNITY_App Entry Point|App Entry Point]]
- [[_COMMUNITY_Backend Tests|Backend Tests]]
- [[_COMMUNITY_Favorit Model|Favorit Model]]
- [[_COMMUNITY_CORS Config|CORS Config]]
- [[_COMMUNITY_Job Offer Controller Layer|Job Offer Controller Layer]]
- [[_COMMUNITY_Business Profile DTO|Business Profile DTO]]
- [[_COMMUNITY_DJ Search DTO|DJ Search DTO]]
- [[_COMMUNITY_Performer Profile DTO|Performer Profile DTO]]
- [[_COMMUNITY_Reservation Response DTO|Reservation Response DTO]]
- [[_COMMUNITY_Equipment Response DTO|Equipment Response DTO]]
- [[_COMMUNITY_Login Request DTO|Login Request DTO]]
- [[_COMMUNITY_Register Business DTO|Register Business DTO]]
- [[_COMMUNITY_Update Performer DTO|Update Performer DTO]]
- [[_COMMUNITY_Business User Model|Business User Model]]
- [[_COMMUNITY_Message Model|Message Model]]
- [[_COMMUNITY_Review Model|Review Model]]
- [[_COMMUNITY_Favorites DTO & Model|Favorites DTO & Model]]
- [[_COMMUNITY_Auth Response DTO|Auth Response DTO]]
- [[_COMMUNITY_Create Equipment DTO|Create Equipment DTO]]
- [[_COMMUNITY_Create Job Offer DTO|Create Job Offer DTO]]
- [[_COMMUNITY_Favorit Response DTO|Favorit Response DTO]]
- [[_COMMUNITY_Job Offer Response DTO|Job Offer Response DTO]]
- [[_COMMUNITY_Register Performer DTO|Register Performer DTO]]
- [[_COMMUNITY_Update Business DTO|Update Business DTO]]
- [[_COMMUNITY_Favorit Repository|Favorit Repository]]
- [[_COMMUNITY_Performer User Model|Performer User Model]]
- [[_COMMUNITY_Equipment Locations Model|Equipment Locations Model]]
- [[_COMMUNITY_Reservation Model|Reservation Model]]
- [[_COMMUNITY_Venue Photos Model|Venue Photos Model]]
- [[_COMMUNITY_Review Repository|Review Repository]]
- [[_COMMUNITY_VSCode Extensions|VSCode Extensions]]
- [[_COMMUNITY_Job Offer Form|Job Offer Form]]
- [[_COMMUNITY_Venue & DJ Browse|Venue & DJ Browse]]
- [[_COMMUNITY_Pricing & Info Pages|Pricing & Info Pages]]
- [[_COMMUNITY_Registration Pages|Registration Pages]]
- [[_COMMUNITY_Booking Management|Booking Management]]
- [[_COMMUNITY_Spring Web & JPA|Spring Web & JPA]]

## God Nodes (most connected - your core abstractions)
1. `Navbar Web Component (navbar.js)` - 15 edges
2. `IzvodacKorisnik Entity` - 9 edges
3. `BeatSyncNavbar` - 8 edges
4. `Auth / Login Page` - 8 edges
5. `DJ Listing/Browse Page` - 8 edges
6. `Favorites Page` - 8 edges
7. `JWT Bearer Token Authentication` - 8 edges
8. `EquipmentController` - 7 edges
9. `Bookings / Reservations Page` - 7 edges
10. `Job Offers Listing Page` - 7 edges

## Surprising Connections (you probably didn't know these)
- `WebSocket` --conceptually_related_to--> `Chat Web Component (chat.js)`  [INFERRED]
  backend/HELP.md → frontend/components/chat.js
- `Spring Security` --implements--> `JWT Bearer Token Authentication`  [INFERRED]
  backend/HELP.md → frontend/bookings.html
- `BeatSyncNavbar Web Component` --conceptually_related_to--> `AuthController REST Controller`  [INFERRED]
  frontend/components/navbar.js → backend/src/main/java/hr/beatsync/backend/controller/AuthController.java
- `How It Works Page` --references--> `Navbar Web Component (navbar.js)`  [EXTRACTED]
  frontend/how-it-works.html → frontend/components/navbar.js
- `Pricing Page` --references--> `Navbar Web Component (navbar.js)`  [EXTRACTED]
  frontend/pricing.html → frontend/components/navbar.js

## Hyperedges (group relationships)
- **Authentication Flow (Login/Register + LocalStorage + JWT)** — auth_html, localstorage_auth, jwt_bearer_auth, api_auth_login, backend_spring_security [INFERRED 0.85]
- **DJ Marketplace Core (DJ Browse + Job Offers + Bookings)** — djs_html, job_offers_html, bookings_html, role_izvodac, role_business [INFERRED 0.85]
- **Shared Navbar Web Component Used Across All Pages** — navbar_js, auth_html, bookings_html, create_equipment_listing_html, create_job_offer_html, djs_html, equipment_html, favorites_html, how_it_works_html, index_html, job_offers_html, pricing_html, profile_business_html, profile_html, register_business_html, register_dj_html [EXTRACTED 1.00]
- **Equipment CRUD Flow (Frontend API + Backend Controller + DTO)** — equipmentapi_renderCreateEquipmentPageApi, controller_EquipmentController, dto_CreateEquipmentRequest [EXTRACTED 1.00]
- **User Registration and Auth Flow** — controller_AuthController, dto_AuthResponse, navbar_BeatSyncNavbar [INFERRED 0.85]
- **Job Offer and Rezervacija Booking Flow** — controller_JobOfferController, controller_RezervacijaController, dto_CreateJobOfferRequest [EXTRACTED 1.00]
- **Reservation Flow: BusinessKorisnik creates JobOffer, IzvodacKorisnik books via Rezervacija** — model_BusinessKorisnik, model_JobOffer, model_IzvodacKorisnik [INFERRED 0.95]
- **IzvodacKorisnik exposed via DjSearchResponse and IzvodacProfilResponse DTOs** — model_IzvodacKorisnik, dto_DjSearchResponse, dto_IzvodacProfilResponse [INFERRED 0.95]
- **Oprema entity, KategorijaOpreme enum, and EquipmentResponse DTO form equipment listing pattern** — model_Oprema, enum_KategorijaOpreme, dto_EquipmentResponse [INFERRED 0.95]
- **JWT Security Pipeline** — security_jwt_authentication_filter, security_jwt_token_provider, security_user_details_service_impl [INFERRED 0.95]
- **Booking and Review Flow** — rezervacija_rezervacija, recenzija_recenzija, model_job_offer [INFERRED 0.85]
- **User Messaging Participants** — poruka_poruka, model_izvodac_korisnik, model_business_korisnik [EXTRACTED 1.00]

## Communities (61 total, 43 thin omitted)

### Community 0 - "Frontend Pages & API Endpoints"
Cohesion: 0.09
Nodes (41): API Endpoint: POST /api/auth/login, API Endpoint: POST /api/auth/register/business, API Endpoint: POST /api/auth/register/izvodac, API Endpoint: GET /api/business/javni/{username}, API Endpoint: GET/PUT /api/business/profil, API Endpoint: GET /api/djs, API Endpoint: GET/POST /api/favoriti, API Endpoint: POST /api/favoriti/toggle (+33 more)

### Community 1 - "Equipment & Favorites Controllers"
Cohesion: 0.09
Nodes (5): EquipmentController, FavoritController, Oprema, FavoritRepository, OpremaRepository

### Community 2 - "Security & Chat Infrastructure"
Cohesion: 0.11
Nodes (21): BeatSyncChat Web Component, CorsConfig Spring Configuration, SecurityConfig Spring Security Configuration, AuthController REST Controller, BusinessController REST Controller, DjController REST Controller, EquipmentController REST Controller, IzvodacController REST Controller (+13 more)

### Community 3 - "Auth & User Repositories"
Cohesion: 0.11
Nodes (3): AuthController, BusinessKorisnikRepository, IzvodacKorisnikRepository

### Community 4 - "Domain Enums & Messaging"
Cohesion: 0.14
Nodes (19): StatusRezervacije Enum, StatusValidacije Enum, VrstaPosiljatelja Enum, BusinessKorisnik Model, IzvodacKorisnik Model, JobOffer Model, Poruka Model, Recenzija Model (+11 more)

### Community 5 - "Response DTOs"
Cohesion: 0.16
Nodes (18): DjSearchResponse DTO, EquipmentResponse DTO, IzvodacProfilResponse DTO, JobOfferResponse DTO, LoginRequest DTO, RegisterBusinessRequest DTO, RegisterIzvodacRequest DTO, RezervacijaResponse DTO (+10 more)

### Community 6 - "Business & DJ Controllers"
Cohesion: 0.13
Nodes (5): BusinessController, IzvodacController, OpremaLokacijaRepository, UserDetailsServiceImpl, UserDetailsService

### Community 7 - "Job Offer Booking Flow"
Cohesion: 0.16
Nodes (3): JobOfferController, JobOffer, JobOfferRepository

### Community 8 - "Equipment Frontend Components"
Cohesion: 0.29
Nodes (8): deleteEquipmentListing(), equipmentApiListings, fetchEquipmentListings(), getEquipmentRole(), getEquipmentToken(), parseEquipmentApiError(), renderCreateEquipmentPageApi(), renderEquipmentPageApi()

### Community 9 - "JWT Security Pipeline"
Cohesion: 0.23
Nodes (3): OncePerRequestFilter, JwtAuthenticationFilter, JwtTokenProvider

### Community 12 - "Equipment Local Storage"
Cohesion: 0.29
Nodes (3): DEFAULT_EQUIPMENT_LISTINGS, ensureSeedData(), getListings()

### Community 14 - "Spring Boot Backend Config"
Cohesion: 0.33
Nodes (6): Backend HELP.md, Spring Boot Backend, Spring Data JPA, Spring Security, Spring Web (REST), WebSocket

### Community 20 - "Equipment Locations"
Cohesion: 0.5
Nodes (4): Oprema Model, OpremaLokacije Model, OpremaLokacijaRepository, OpremaRepository

### Community 25 - "Job Offer Controller Layer"
Cohesion: 0.67
Nodes (3): JobOfferController REST Controller, RezervacijaController REST Controller, CreateJobOfferRequest DTO

## Knowledge Gaps
- **72 isolated node(s):** `recommendations`, `AuthResponse`, `CreateEquipmentRequest`, `CreateJobOfferRequest`, `EquipmentResponse` (+67 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `RezervacijaRepository` connect `Reservation Controller` to `Job Offer Booking Flow`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `IzvodacKorisnik Entity` (e.g. with `RegisterIzvodacRequest DTO` and `UpdateIzvodacRequest DTO`) actually correct?**
  _`IzvodacKorisnik Entity` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `DJ Listing/Browse Page` (e.g. with `DJ Performer Profile Page` and `Equipment Rental Browse Page`) actually correct?**
  _`DJ Listing/Browse Page` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `recommendations`, `AuthResponse`, `CreateEquipmentRequest` to the rest of the system?**
  _72 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Frontend Pages & API Endpoints` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Equipment & Favorites Controllers` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Security & Chat Infrastructure` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._