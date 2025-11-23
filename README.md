# asd-cointrack-api

ASD Coinventory API è un servizio REST per la gestione e la catalogazione di un inventario di monete numismatiche.  
Espone endpoint CRUD, funzionalità di ricerca avanzata, statistiche, raggruppamento in collezioni e metadati di audit.  
Utilizza MongoDB come data store e integra un set completo di test automatici e analisi statiche (JUnit, Testcontainers, SpotBugs, PMD), pensato per pipeline di Automated Software Delivery (ASD) e contesti CI/CD.

Ideale come backend centralizzato per applicazioni, automazioni e sistemi di gestione numismatica.

---

## 1. Architettura e Stack Tecnologico

- **Linguaggio**: Java 17  
- **Framework**: Spring Boot 3.4.x  
- **Database**: MongoDB  
- **Persistenza**: Spring Data MongoDB  
- **Testing**:
  - JUnit 5
  - Spring Boot Test
  - Testcontainers (MongoDB)
- **Analisi statica**:
  - SpotBugs
  - PMD
- **Build tool**: Maven (wrapper `mvnw`)  
- **Containerizzazione**: Docker + Docker Compose  
- **CI**: GitHub Actions (`.github/workflows/ci.yml`)

### 1.1 Struttura del Repository

- Root del repo: `asd-cointrack-api/`
- Modulo Spring Boot: `asd-cointrack-api/asd-cointrack-api/`

Tutti i comandi Maven riportati di seguito vanno eseguiti da:

```bash
cd asd-cointrack-api/asd-cointrack-api
```

---

## 2. Modello di Dominio

### 2.1 Entità `Coin`

Classe: `com.asd.cointrack.model.Coin`

Rappresenta una moneta numismatica. Campi principali:

- Identità e base:
  - `ID` (String) – identificativo univoco generato da MongoDB.
  - `name` (String) – nome della moneta (max 100 caratteri).
  - `year` (int) – anno di conio (`0 <= year <= anno corrente`).
  - `material` (String) – materiale (max 100 caratteri).
- Dati fisici:
  - `weight` (double) – peso (> 0).
  - `diameter` (double) – diametro (> 0).
  - `height` (double) – spessore (> 0).
- Dati economici:
  - `price` (double) – valore stimato (>= 0).
- Conservazione e rarità:
  - `conservationObverse` (enum `OptionConservation`) – stato del dritto.
  - `conservationReverse` (enum `OptionConservation`) – stato del rovescio.
  - `degree` (enum `NumismaticRarity`) – grado di rarità numismatica.
- Altri metadati:
  - `note` (String) – note libere (max 1000 caratteri).
  - `photoPathObverse` (String) – path/fonte foto dritto.
  - `photoPathReverse` (String) – path/fonte foto rovescio.
  - `collectionId` (String) – id della collezione di appartenenza (facoltativo).

Campi indicizzati in MongoDB:

- `year`, `material`, `price`, `degree`, `collectionId`.

Audit automatico (Spring Data MongoDB):

- `createdAt`, `updatedAt` (Instant)
- `createdBy`, `updatedBy` (String)

### 2.2 Enum `OptionConservation`

Classe: `com.asd.cointrack.model.OptionConservation`  
Stati di conservazione:

`FDC, qFDC, SPL, qSPL, BB, qBB, MB, B, D, ILLEGIBILE`

### 2.3 Enum `NumismaticRarity`

Classe: `com.asd.cointrack.model.NumismaticRarity`  
Gradi di rarità:

`R5, R4, R3, R2, R, NC, C`

### 2.4 Entità `CoinCollection`

Classe: `com.asd.cointrack.model.CoinCollection`

Rappresenta una collezione / categoria (es. “Regno d’Italia”, “Euro commemorativi”).

Campi:

- `id` (String) – ID univoco.
- `name` (String) – nome collezione (max 100, univoco).
- `description` (String) – descrizione estesa (max 1000).
- campi di audit: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.

---

## 3. Endpoint REST

- **Base URL (locale)**: `http://localhost:8080`
- **Controller principali**:
  - `com.asd.cointrack.controller.CoinController` – monete (`/api/coins`)
  - `com.asd.cointrack.controller.CoinCollectionController` – collezioni (`/api/collections`)

### 3.1 Monete – CRUD e ricerca (`/api/coins`)

#### 3.1.1 Lista paginata

`GET /api/coins`

Parametri:

- `page` (int, opzionale, default 0)
- `size` (int, opzionale, default 20, max 100)
- `sort` (string, opzionale, es. `name,asc` / `year,desc` / `price,desc`)

Restituisce una `Page<Coin>` con:

- contenuto paginato,
- informazioni su pagina corrente, totale elementi e pagine.

#### 3.1.2 Dettaglio moneta

`GET /api/coins/{id}`

- `200 OK` con body `Coin` se l’ID esiste.
- `404 Not Found` se l’ID non esiste.

#### 3.1.3 Audit moneta

`GET /api/coins/{id}/audit`

Restituisce un `CoinAuditInfo` con:

- `id`, `name`
- `createdAt`, `updatedAt`
- `createdBy`, `updatedBy`

#### 3.1.4 Ricerca base

`GET /api/coins/search`

Parametri:

- `name` (String, opzionale) – esatta corrispondenza del nome.
- `year` (Integer, opzionale) – anno esatto.
- `page`, `size`, `sort` come sopra.

Se `name` è presente → filtra per nome, altrimenti se `year` è presente → filtra per anno, altrimenti restituisce tutte le monete (paginato).

#### 3.1.5 Ricerca avanzata

`GET /api/coins/advanced-search`

Filtri supportati:

- Testo (case-insensitive, “contains”):
  - `name` – frammento del nome.
  - `material` – frammento del materiale.
- Enum:
  - `degree` – singola rarità.
  - `degreeIn` – lista di rarità, es. `degreeIn=R3,R4,R5`.
  - `conservationObverseIn` – lista di stati di conservazione per il dritto, es. `conservationObverseIn=SPL,FDC`.
- Range:
  - `minYear`, `maxYear` (>= 0, con vincolo `minYear <= maxYear` se entrambi presenti).
  - `minPrice`, `maxPrice` (>= 0.0, con vincolo `minPrice <= maxPrice`).
- Paginazione/ordinamento: `page`, `size`, `sort`.

Esempio:

```http
GET /api/coins/advanced-search?name=Euro&degreeIn=R3,R4&minYear=1900&maxYear=1950&minPrice=50&sort=price,desc
```

Errori:

- Se `minYear > maxYear` o `minPrice > maxPrice` → `400 Bad Request` con messaggio chiaro.

#### 3.1.6 Creazione moneta

`POST /api/coins`

Body JSON (esempio):

```json
{
  "name": "2 Euro Italia",
  "year": 2002,
  "material": "Bimetal",
  "weight": 8.5,
  "diameter": 25.75,
  "height": 2.2,
  "price": 10.0,
  "conservationObverse": "SPL",
  "conservationReverse": "SPL",
  "degree": "C",
  "note": "Prima emissione",
  "photoPathObverse": "/img/2euro-obv.jpg",
  "photoPathReverse": "/img/2euro-rev.jpg",
  "collectionId": null
}
```

`ID`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy` sono gestiti lato server.

Risposta:

- `201 Created` con `Coin` creato.
- `400 Bad Request` se la validazione fallisce (vedi sezione errori).

#### 3.1.7 Aggiornamento moneta

`PUT /api/coins/{id}`

- Body come nel `POST` (esclusi campi di audit).
- `200 OK` con `Coin` aggiornato.
- `404 Not Found` se l’ID non esiste.

#### 3.1.8 Cancellazione moneta

`DELETE /api/coins/{id}`

- `204 No Content`, anche se la moneta non esiste (idempotente).

---

### 3.2 Statistiche (`/api/coins/stats`)

#### 3.2.1 Summary globale

`GET /api/coins/stats/summary`

Restituisce:

- `totalCount` – numero totale di monete.
- `totalPrice` – somma dei prezzi.
- `averagePrice` – prezzo medio.
- `minYear`, `maxYear` – anno minimo/massimo.

#### 3.2.2 Distribuzione per anno

`GET /api/coins/stats/by-year`  
Restituisce una lista di `{ "year": ..., "count": ... }`.

#### 3.2.3 Distribuzione per rarità

`GET /api/coins/stats/by-degree`  
Restituisce una lista di `{ "degree": "R3", "count": ... }`.

#### 3.2.4 Distribuzione per materiale

`GET /api/coins/stats/by-material`  
Restituisce una lista di `{ "material": "Argento", "count": ... }`.

#### 3.2.5 Monete più costose

`GET /api/coins/stats/top-expensive?limit=N`

- `limit` (int, default `10`, `1 <= N <= 100`).
- Restituisce la lista delle monete ordinate per `price` DESC.

---

### 3.3 Collezioni / categorie (`/api/collections`)

#### 3.3.1 Lista collezioni

`GET /api/collections`  
Restituisce tutte le collezioni definite.

#### 3.3.2 Dettaglio collezione

`GET /api/collections/{id}`  
- `200 OK` con `CoinCollection` se esiste.  
- `404 Not Found` se non esiste.

#### 3.3.3 Creazione collezione

`POST /api/collections`

Body (esempio):

```json
{
  "name": "Euro commemorativi",
  "description": "Collezione di monete da 2 euro commemorative"
}
```

Risposta:

- `201 Created` con `CoinCollection` creata.
- `400 Bad Request` se `name` è vuoto o supera 100 caratteri.

#### 3.3.4 Monete di una collezione

`GET /api/collections/{id}/coins`

Parametri:

- `page`, `size`, `sort` come per `/api/coins`.

Risposta:

- `200 OK` con `Page<Coin>` delle monete appartenenti alla collezione.
- `404 Not Found` se la collezione non esiste.

---

## 4. Validazione e gestione errori

La validazione usa `jakarta.validation` su:

- model `Coin` e `CoinCollection`,
- parametri di query (es. `minYear`, `maxYear`, `limit`).

Gli errori vengono gestiti da `GlobalExceptionHandler` e restituiti come `400 Bad Request` con JSON strutturato.

Esempio di risposta per errore di validazione body:

```json
{
  "status": 400,
  "error": "Validation failed",
  "errors": [
    { "field": "name", "message": "name must not be blank" },
    { "field": "year", "message": "year must not be in the future" }
  ]
}
```

Esempio per parametri non validi (range):

```json
{
  "status": 400,
  "error": "Invalid request",
  "message": "minPrice must be less than or equal to maxPrice"
}
```

---

## 5. Avvio dell’Applicazione

### 5.1 API + MongoDB via Docker Compose

Prerequisito: Docker Desktop in esecuzione.

```bash
cd asd-cointrack-api/asd-cointrack-api
docker compose up -d
```

Questo comando avvia:

- **MongoDB**:
  - image: `mongo:latest`
  - database: `mydatabase`
  - username: `root`
  - password: `secret`
- **API**:
  - container che espone l’app sulla porta `8080`
  - configurata con  
    `SPRING_DATA_MONGODB_URI=mongodb://root:secret@mongodb:27017/mydatabase?authSource=admin`

Per arrestare i container:

```bash
docker compose down
```

### 5.2 MongoDB in Docker, API in locale con Maven

1. Avvia MongoDB:

```bash
docker run --name cointrack-mongodb -d \
  -e MONGO_INITDB_DATABASE=mydatabase \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=secret \
  -p 27017:27017 mongo:latest
```

2. Avvia l’API con Maven:

```bash
cd asd-cointrack-api/asd-cointrack-api
SPRING_DATA_MONGODB_URI="mongodb://root:secret@localhost:27017/mydatabase?authSource=admin" \
  ./mvnw spring-boot:run
```

API disponibile su: `http://localhost:8080`.

---

## 6. Test Automatici e Analisi di Qualità

> Tutti i comandi vanno eseguiti in `asd-cointrack-api/asd-cointrack-api`.

L’obiettivo della suite è garantire che:

- l’applicazione si avvii correttamente contro un MongoDB reale (Testcontainers),
- il comportamento di base sia integro ad ogni modifica,
- il codice mantenga un livello minimo di qualità (assenza di bug evidenti e code smell gravi).

Per ogni tipologia trovi di seguito: **quando usarla**, **come eseguirla**, **cosa verifica** e **cosa produce**.

### 6.1 Test di integrazione (JUnit + Spring Boot Test + Testcontainers)

- **Quando usarli**
  - Durante lo sviluppo, dopo modifiche al dominio/servizi/repository.
  - Quando vuoi verificare che l’app si avvii e parli correttamente con MongoDB.

- **Comando**

  ```bash
  ./mvnw test
  ```

- **Cosa verificano**
  - Compilazione ed esecuzione dei test in `src/test/java`.
  - Avvio di un container Docker MongoDB tramite Testcontainers.
  - Caricamento del contesto Spring Boot (`@SpringBootTest`).
  - Capacità dell’app di connettersi e lavorare con un database reale.

- **Cosa restituiscono**
  - In console: riepilogo dettagliato dei test (eseguiti, falliti, errori, skip, tempo totale).
  - File di report JUnit in `target/surefire-reports/` (XML + TXT), utili per CI e strumenti di reporting.

### 6.2 Quality Gate completo: build + test + SpotBugs + PMD

- **Quando usarlo**
  - Prima di ogni commit/push importante.
  - In CI (job di build principale).

- **Comando**

  ```bash
  ./mvnw clean verify
  ```

- **Cosa verifica**
  - Pulizia (`clean`) della cartella `target/`.
  - Compilazione main e test (`compile`, `test-compile`).
  - Esecuzione dei test di integrazione (`test`).
  - Creazione del jar eseguibile (`package`).
  - Analisi SpotBugs (`spotbugs:check`) sui bytecode, con:
    - `effort=Max`, `threshold=Low`, `failOnError=true`.
  - Analisi PMD (`pmd:check`) sul sorgente Java con le regole default di PMD 7.x.

- **Cosa restituisce**
  - Exit code 0 (build verde) se:
    - tutti i test passano,
    - SpotBugs non trova bug,
    - PMD non trova violazioni bloccanti.
  - Exit code diverso da 0 (build rossa) se:
    - almeno un test fallisce,
    - SpotBugs rileva bug considerati errori,
    - PMD rileva violazioni rispetto alle regole attive.
  - Report generati:
    - Test → `target/surefire-reports/`
    - SpotBugs → `target/spotbugsXml.xml`
    - PMD → `target/reports/pmd.html` (HTML) e `target/pmd/pmd.xml` (XML).

### 6.3 Solo analisi statica (SpotBugs + PMD)

Questi comandi sono utili quando vuoi concentrarti solo sulla qualità del codice, senza attendere l’esecuzione dei test.

- **SpotBugs – solo report**

  - **Comando**

    ```bash
    ./mvnw spotbugs:spotbugs
    ```

  - **Cosa verifica**
    - Analisi dei bytecode alla ricerca di bug potenziali (null pointer, esposizione di stato interno, problemi di concorrenza, ecc.).

  - **Cosa restituisce**
    - Console: numero di bug trovati e categorie.
    - File: `target/spotbugsXml.xml` con dettagli per classe/metodo.

- **SpotBugs – quality gate**

  - **Comando**

    ```bash
    ./mvnw spotbugs:check
    ```

  - **Cosa verifica**
    - Stessa analisi di `spotbugs:spotbugs`, ma fallisce la build se i bug non sono zero.

- **PMD – solo report**

  - **Comando**

    ```bash
    ./mvnw pmd:pmd
    ```

  - **Cosa verifica**
    - Sorgente Java rispetto a regole di stile, complessità, best practice.

  - **Cosa restituisce**
    - Console: riepilogo violazioni.
    - HTML: `target/reports/pmd.html` (navigabile per file e regola).
    - XML: `target/pmd/pmd.xml`.

- **PMD – quality gate**

  - **Comando**

    ```bash
    ./mvnw pmd:check
    ```

  - **Cosa verifica**
    - Stesse regole di `pmd:pmd`, ma fallisce la build se ci sono violazioni considerate bloccanti.

- **Analisi statica senza eseguire i test**

  - **Comando**

    ```bash
    ./mvnw -DskipTests spotbugs:check pmd:check
    ```

  - **Uso tipico**
    - Iterazione veloce su code quality mentre si sviluppa, quando i test sono pesanti (Testcontainers).
  - **Output**
    - Build verde/rossa in base a SpotBugs/PMD, senza esecuzione dei test JUnit.

---

## 7. Linee Guida di Utilizzo dei Test

- **Durante lo sviluppo**:
  - `./mvnw test` per verificare l’avvio dell’app e l’integrazione con MongoDB.
  - `./mvnw -DskipTests spotbugs:check pmd:check` per controllare rapidamente la qualità del codice.
- **Prima di commit/push o di aprire una Pull Request**:
  - `./mvnw clean verify` per eseguire l’intera pipeline (build + test + SpotBugs + PMD), allineata alla CI.

---

## 8. Esempi cURL

Questa sezione mostra esempi pratici di chiamate cURL verso i principali endpoint, con indicazione dei parametri, dei codici di risposta attesi e degli errori più comuni.

> Tutti gli esempi assumono che l’API sia disponibile su `http://localhost:8080`.

### 8.1 Creazione di una moneta

Endpoint: `POST /api/coins`

```bash
curl -X POST "http://localhost:8080/api/coins" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "2 Euro Italia",
    "year": 2002,
    "material": "Bimetal",
    "weight": 8.5,
    "diameter": 25.75,
    "height": 2.2,
    "price": 10.0,
    "conservationObverse": "SPL",
    "conservationReverse": "SPL",
    "degree": "C",
    "note": "Prima emissione",
    "photoPathObverse": "/img/2euro-obv.jpg",
    "photoPathReverse": "/img/2euro-rev.jpg",
    "collectionId": null
  }'
```

**Risposta attesa**

- `201 Created` con body JSON della moneta creata, incluso:
  - campo `id` (o `ID`) popolato,
  - campi di audit (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).

**Errori possibili**

- `400 Bad Request` se la validazione fallisce, ad esempio:
  - `name` vuoto o oltre 100 caratteri;
  - `year` futuro o negativo;
  - `weight/diameter/height <= 0`;
  - `price < 0`;
  - `conservationObverse`, `conservationReverse`, `degree` null o con valore enum non valido.

Il payload di errore riporta sempre:

```json
{
  "status": 400,
  "error": "Validation failed",
  "errors": [
    { "field": "nomeCampo", "message": "messaggio di errore" }
  ]
}
```

### 8.2 Lista paginata di monete

Endpoint: `GET /api/coins`

```bash
curl "http://localhost:8080/api/coins?page=0&size=20&sort=year,desc"
```

**Parametri**

- `page` – indice di pagina (0-based).
- `size` – elementi per pagina (default 20, max 100).
- `sort` – campo e direzione, es. `name,asc`, `year,desc`, `price,desc`.

**Risposta attesa**

- `200 OK` con struttura paginata, ad esempio:

```json
{
  "content": [ { ... }, { ... } ],
  "pageable": { ... },
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

**Errori possibili**

- `400 Bad Request` se i parametri violano i vincoli di tipo (es. `size` non numerico).  
  I messaggi arrivano come errori di constraint (vedi sezione 4).

### 8.3 Ricerca base per nome o anno

Endpoint: `GET /api/coins/search`

```bash
# Ricerca per nome esatto
curl "http://localhost:8080/api/coins/search?name=2%20Euro%20Italia&page=0&size=10"

# Ricerca per anno
curl "http://localhost:8080/api/coins/search?year=2002&page=0&size=10"
```

**Comportamento**

- Se `name` è specificato → match per nome esatto.
- Altrimenti, se `year` è specificato → match per anno.
- Se nessuno dei due è specificato → equivalente a `GET /api/coins`.

**Risposta**

- `200 OK` con pagina di monete.

### 8.4 Ricerca avanzata

Endpoint: `GET /api/coins/advanced-search`

Esempio di ricerca complessa:

```bash
curl "http://localhost:8080/api/coins/advanced-search\
?name=Euro\
&material=argento\
&degreeIn=R3,R4,R5\
&conservationObverseIn=SPL,FDC\
&minYear=1900\
&maxYear=1950\
&minPrice=50\
&maxPrice=500\
&page=0\
&size=20\
&sort=price,desc"
```

**Cosa fa**

- Filtra per:
  - nome contenente “Euro” (case-insensitive),
  - materiale contenente “argento” (case-insensitive),
  - rarità in `{R3, R4, R5}`,
  - conservazione del dritto in `{SPL, FDC}`,
  - anno tra 1900 e 1950,
  - prezzo tra 50 e 500.
- Ordina per `price` DESC, restituisce la prima pagina di 20 elementi.

**Errori possibili**

- `400 Bad Request` se:
  - `minYear > maxYear`,
  - `minPrice > maxPrice`,
  - uno dei parametri che rappresentano enum (`degree`, `degreeIn`, `conservationObverseIn`) contiene un valore non valido.

Nel caso di range non validi viene restituito:

```json
{
  "status": 400,
  "error": "Invalid request",
  "message": "minPrice must be less than or equal to maxPrice"
}
```

### 8.5 Statistiche

#### 8.5.1 Summary globale

```bash
curl "http://localhost:8080/api/coins/stats/summary"
```

Risposta:

```json
{
  "totalCount": 42,
  "totalPrice": 12345.0,
  "averagePrice": 293.0,
  "minYear": 1850,
  "maxYear": 2024
}
```

#### 8.5.2 Distribuzione per anno / rarità / materiale

```bash
curl "http://localhost:8080/api/coins/stats/by-year"
curl "http://localhost:8080/api/coins/stats/by-degree"
curl "http://localhost:8080/api/coins/stats/by-material"
```

Risposta tipica (by-year):

```json
[
  { "year": 1850, "count": 2 },
  { "year": 1900, "count": 5 },
  { "year": 2002, "count": 10 }
]
```

#### 8.5.3 Monete più costose

```bash
curl "http://localhost:8080/api/coins/stats/top-expensive?limit=5"
```

Risposta:

- `200 OK` con lista ordinata per `price` DESC.
- `400 Bad Request` se `limit` è fuori range (`limit < 1` o `limit > 100`).

### 8.6 Collezioni

#### 8.6.1 Creazione di una collezione

Endpoint: `POST /api/collections`

```bash
curl -X POST "http://localhost:8080/api/collections" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Euro commemorativi",
    "description": "Collezione di monete da 2 euro commemorative"
  }'
```

Risposta:

- `201 Created` con oggetto `CoinCollection` (inclusi campi di audit).
- `400 Bad Request` se `name` è vuoto o oltre 100 caratteri.

#### 8.6.2 Lista collezioni

```bash
curl "http://localhost:8080/api/collections"
```

Risposta:

- `200 OK` con array di `CoinCollection`.

#### 8.6.3 Monete di una collezione

```bash
curl "http://localhost:8080/api/collections/{collectionId}/coins?page=0&size=20&sort=name,asc"
```

Sostituisci `{collectionId}` con l’`id` ottenuto dal `POST` o da `GET /api/collections`.

Risposta:

- `200 OK` con `Page<Coin>` appartenenti alla collezione.
- `404 Not Found` se la collezione non esiste.
