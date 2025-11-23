# ASD Cointrack API – Architecture Overview

ASD Cointrack API è un backend REST basato su Spring Boot e MongoDB, progettato con una struttura a livelli chiara, validazione robusta e controlli di qualità integrati (test di integrazione, SpotBugs, PMD, CI GitHub Actions).

Questo documento descrive:

- la struttura dei pacchetti e dei livelli applicativi,
- il flusso delle richieste,
- il modello dati e la persistenza,
- auditing, validazione, gestione errori e qualità del codice.

---

## 1. Struttura dei pacchetti

Tutto il codice applicativo risiede sotto:  
`asd-cointrack-api/asd-cointrack-api/src/main/java/com/asd/cointrack`

Struttura principale:

- `AsdCointrackApiApplication` – entrypoint Spring Boot.
- `controller` – layer di esposizione REST (CoinController, CoinCollectionController).
- `service` – layer di business e orchestrazione (CoinService, CoinCollectionService).
- `repository` – layer di persistenza (MongoDB via Spring Data).
- `model` – entità dominio e enum (`Coin`, `CoinCollection`, `OptionConservation`, `NumismaticRarity`).
- `dto` – Data Transfer Objects per statistiche e audit.
- `validation` – annotazioni e validator custom (`MaxCurrentYear`).
- `exception` – gestione centralizzata delle eccezioni REST.
- `config` – configurazione tecnica (auditing MongoDB, ecc.).

Test e risorse:

- `src/test/java/com/asd/cointrack` – test di integrazione con Spring Boot e Testcontainers.
- `src/main/resources` – configurazione applicativa (es. `application.properties`).

---

## 2. Livelli applicativi e responsabilità

### 2.1 Controller (API Layer)

Pacchetto: `com.asd.cointrack.controller`

Responsabilità:

- Esporre endpoint REST (HTTP) verso i client.
- Validare input a livello di parametri (`@Validated`, `@RequestParam`, `@PathVariable`, `@Valid`).
- Convertire le risposte dei service in `ResponseEntity`.
- Non contiene logica di business complessa.

Controller chiave:

- `CoinController` – gestione monete:
  - CRUD (`/api/coins`),
  - ricerca base (`/search`) e avanzata (`/advanced-search`),
  - statistiche (`/stats/...`),
  - audit (`/{id}/audit`).
- `CoinCollectionController` – gestione collezioni:
  - CRUD base sulle collezioni (`/api/collections`),
  - monete per collezione (`/api/collections/{id}/coins`).

### 2.2 Service (Business Layer)

Pacchetto: `com.asd.cointrack.service`

Responsabilità:

- Incapsulare la logica di business e le regole applicative.
- Coordinare repository, MongoTemplate e DTO.
- Applicare logica di paginazione e sanitizzazione dei parametri (`sanitizePageable`).
- Verificare la correttezza di range e combinazioni di filtri (`validateRanges`).

Service principali:

- `CoinService`
  - Gestione CRUD monete.
  - Ricerca base e avanzata con combinazione di filtri su testo, enum e range.
  - Statistiche aggregate (summary, by-year, by-degree, by-material, top-expensive).
  - Costruzione DTO di audit per le monete.
- `CoinCollectionService`
  - Gestione CRUD collezioni.
  - Recupero paginato delle monete appartenenti a una collezione.

### 2.3 Repository (Persistence Layer)

Pacchetto: `com.asd.cointrack.repository`

Responsabilità:

- Astrazione sopra MongoDB tramite Spring Data.
- Definizione di metodi di query tipizzati (derivati da naming convention o annotazioni `@Query`).

Repository:

- `CoinRepository extends MongoRepository<Coin, String>`
  - metodi standard `findAll`, `findById`, `save`, `deleteById`;
  - metodi derivati:
    - `findByName`, `findByYear`, `findByMaterial`, ecc. (List e Page);
    - range numerici (`findByWeightBetween`, `findByDiameterBetween`, `findByHeightBetween`, `findByPriceBetween`);
    - `findByCollectionId` per raggruppamento per collezione.
- `CoinCollectionRepository extends MongoRepository<CoinCollection, String>`
  - gestione delle collezioni.

Per le ricerche avanzate (combinazione di molti filtri) viene usato `MongoTemplate` in `CoinService`.

### 2.4 Modello di dominio

Pacchetto: `com.asd.cointrack.model`

Entità principali:

- `Coin`
  - rappresenta la moneta con dati fisici, economici, di conservazione e rarità;
  - include riferimenti alla collezione (`collectionId`) e metadati di audit;
  - diversi campi sono indicizzati per migliorare query e ordinamento (`year`, `material`, `price`, `degree`, `collectionId`).
- `CoinCollection`
  - rappresenta un gruppo logico di monete (categoria/collezione).

Enum:

- `OptionConservation` – valori di conservazione (FDC, SPL, BB, ecc.).
- `NumismaticRarity` – gradi di rarità (R5…C).

### 2.5 DTO

Pacchetto: `com.asd.cointrack.dto`

DTO utilizzati per:

- Statistiche:
  - `CoinsSummaryStats` – summary globale su tutte le monete.
  - `CoinsByYearStats` – numero di monete per anno.
  - `CoinsByDegreeStats` – numero di monete per rarità.
  - `CoinsByMaterialStats` – numero di monete per materiale.
- Audit:
  - `CoinAuditInfo` – metadati di creazione/ultima modifica di una moneta.

I DTO sono usati come contract esterno per evitare di esporre l’intera entity in scenari di aggregazione/monitoraggio.

### 2.6 Validazione e gestione errori

Pacchetti:

- `com.asd.cointrack.validation`
- `com.asd.cointrack.exception`

Componenti:

- `MaxCurrentYear` / `MaxCurrentYearValidator`
  - constraint custom che impone `year <= anno corrente`.
- Validazioni `jakarta.validation` su:
  - `Coin` e `CoinCollection` (vincoli su stringhe, numerici, enum non null).
  - parametri di query (`@Min`, `@DecimalMin`, `@Max`, ecc.).
- `GlobalExceptionHandler` (`@RestControllerAdvice`)
  - intercetta e trasforma:
    - `MethodArgumentNotValidException` (body non valido),
    - `ConstraintViolationException` (parametri invalidi),
    - `IllegalArgumentException` (range logici non validi),
  - in risposte `400 Bad Request` con JSON strutturato (status, error, lista errori o message).

### 2.7 Configurazione tecnica

Pacchetto: `com.asd.cointrack.config`

- `MongoAuditingConfig`
  - abilita `@EnableMongoAuditing`;
  - definisce un `AuditorAware<String>` che (per ora) restituisce `"system"` come utente tecnico;
  - consente la gestione automatica di `createdAt`, `updatedAt`, `createdBy`, `updatedBy` su entità annotate.

---

## 3. Flusso di una richiesta tipica

Di seguito due esempi di flusso: una ricerca avanzata e la creazione di una moneta.

### 3.1 Flusso: ricerca avanzata (`GET /api/coins/advanced-search`)

1. Il client invia una richiesta HTTP con query param (es. `name`, `material`, `degreeIn`, `minYear`, `maxYear`, `minPrice`, `maxPrice`, `page`, `size`, `sort`).
2. `CoinController.advancedSearch(...)`:
   - valida i parametri (es. `@Min`, `@DecimalMin`);
   - costruisce un `Pageable` preconfigurato;
   - delega a `CoinService.advancedSearch(...)`.
3. `CoinService.advancedSearch(...)`:
   - chiama `validateRanges(...)` per garantire che `minYear <= maxYear` e `minPrice <= maxPrice`;
   - chiama `sanitizePageable(...)` per normalizzare `page` e `size`;
   - costruisce un `Query` e `Criteria` dinamici con tutte le condizioni di filtro:
     - regex case-insensitive per testo;
     - filtri `in` per liste di enum;
     - filtri `gte`/`lte` per range numerici;
   - usa `MongoTemplate` per:
     - contare i risultati (`count`),
     - recuperare la pagina (`find` con `query.with(pageable)`).
4. Il risultato viene incapsulato in un `PageImpl<Coin>` e ritornato al controller.
5. Il controller restituisce la pagina come JSON.
6. In caso di range non validi o parametri fuori vincolo:
   - viene lanciata `IllegalArgumentException` / `ConstraintViolationException`,
   - `GlobalExceptionHandler` converte l’eccezione in `400 Bad Request` con JSON descrittivo.

### 3.2 Flusso: creazione moneta (`POST /api/coins`)

1. Il client invia una richiesta HTTP con JSON nel body.
2. `CoinController.createCoin(@Valid @RequestBody Coin)`:
   - attiva la validazione JSR-380 sui campi di `Coin` (name, year, material, ecc.);
   - in caso di errori, `MethodArgumentNotValidException` viene intercettata da `GlobalExceptionHandler`.
3. Se la validazione ha successo:
   - il controller delega a `CoinService.createCoin(...)`.
4. `CoinService.createCoin(...)`:
   - invoca `coinRepository.save(coin)`;
   - Spring Data Mongo:
     - inserisce il documento in MongoDB,
     - popola i campi di audit (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).
5. Il controller restituisce `201 Created` con il documento `Coin` completo.

---

## 4. Persistenza e auditing

### 4.1 MongoDB

- Entità principali mappate con `@Document`:
  - `Coin` → `coins`
  - `CoinCollection` → `collections`
- Indici:
  - `Coin` ha indici su `year`, `material`, `price`, `degree`, `collectionId` per supportare ricerche e sorting efficienti.
  - `CoinCollection` ha un indice univoco su `name`.

### 4.2 Auditing

Grazie a `@EnableMongoAuditing` e alle annotazioni:

- `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`:
  - i campi di audit sono popolati automaticamente alla creazione e ad ogni update;
- `MongoAuditingConfig.auditorProvider()`:
  - fornisce l’identità dell’auditor (`"system"`), sostituibile in futuro con l’utente autenticato.

Endpoint dedicato: `GET /api/coins/{id}/audit`  
permette di esporre in modo sicuro solo i metadati di audit di una moneta.

---

## 5. Testing e qualità del codice

Il progetto adotta una strategia di qualità multilivello:

- Test di integrazione con Spring Boot + Testcontainers:
  - garantiscono che l’applicazione funzioni contro un MongoDB reale.
- Analisi statica con SpotBugs e PMD:
  - individuano bug potenziali e code smell.
- Quality gate Maven (`./mvnw clean verify`):
  - vincola l’integrazione di nuove modifiche al superamento dei test e dei controlli di qualità.
- CI GitHub Actions:
  - esegue `clean verify` su ogni push/PR, assicurando coerenza tra sviluppo locale e pipeline remota.

Per i dettagli dei comandi e dei report generati, vedere la sezione **6. Test Automatici e Analisi di Qualità** del `README.md`.

---

## 6. Estendibilità

L’architettura attuale è pensata per essere estendibile:

- Aggiunta di nuovi endpoint:
  - si inseriscono nel controller corrispondente, delegando la logica ai service,
  - si mantengono i repository come semplice layer di persistenza.
- Aggiunta di nuovi tipi di statistiche:
  - si implementano nel `CoinService` usando `MongoTemplate` o query aggregazione,
  - si espongono tramite nuovi DTO in `com.asd.cointrack.dto`.
- Integrazione sicurezza:
  - si può introdurre Spring Security senza impattare la struttura a livelli,
  - `MongoAuditingConfig` può essere aggiornato per usare l’utente autenticato come auditor.

In sintesi, ASD Cointrack API adotta un design a livelli chiaro, supportato da validazione, auditing e controlli di qualità, per facilitare manutenzione ed evoluzione nel tempo.

