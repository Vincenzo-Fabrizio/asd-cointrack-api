# asd-cointrack-api
ASD Coinventory API è un servizio REST pensato per gestire e catalogare un inventario di monete. Fornisce endpoint CRUD, struttura scalabile e integrazione con pipeline Automated Software Delivery (ASD). Ideale come backend centralizzato per app, automazioni e sistemi numismatici.

Il progetto include anche un set completo di test automatici e analisi statiche del codice (JUnit, Testcontainers, SpotBugs, PMD), eseguibili tramite Maven, pensati per garantire qualità e stabilità dell’API:

- **Test di integrazione (JUnit 5 + Spring Boot Test + Testcontainers)**  
  Verificano l’avvio corretto dell’applicazione e l’integrazione con MongoDB in ambiente Dockerizzato.  
  Restituiscono log dettagliati di esecuzione e report JUnit in `target/surefire-reports/` (file `.xml` e `.txt`).

- **Analisi statica con SpotBugs**  
  Analizza i bytecode alla ricerca di bug potenziali (null pointer, esposizione di stato interno, problemi di concorrenza, ecc.).  
  Restituisce un report dei bug rilevati in `target/spotbugsXml.xml` e può far fallire la build in caso di violazioni.

- **Analisi di stile e code smell con PMD**  
  Controlla il sorgente Java rispetto a regole di stile, best practice e complessità del codice.  
  Restituisce un report HTML navigabile in `target/reports/pmd.html` e un report XML in `target/pmd/pmd.xml`, utilizzati come quality gate in fase di build.

- **Quality gate unificato (`./mvnw clean verify`)**  
  Esegue in sequenza compilazione, test JUnit con Testcontainers, analisi SpotBugs e PMD, fungendo da verifica completa prima di rilasci o integrazioni CI/CD.
