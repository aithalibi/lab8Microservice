# TP 8 : Spring @RestController — API REST Banque (JSON & XML)

Implémentation du TP 8 du cours **« Architecture Microservices : Conception, Déploiement et Orchestration »** (MLIAEdu).

L'objectif est de construire un **service REST** complet avec **Spring Boot** exposant la ressource
`Compte` : opérations **CRUD** (Create / Read / Update / Delete), sérialisation **JSON et XML**,
persistance **H2** en mémoire, documentation interactive **Swagger / OpenAPI** et tests d'intégration.

---

## 1. Objectif du TP

| Élément | Contenu |
|---|---|
| Type de service | REST (Spring MVC, `@RestController`) |
| Ressource | `Compte` (id, solde, dateCreation, type) |
| Formats | `application/json` **et** `application/xml` |
| Base de données | H2 en mémoire (`jdbc:h2:mem:banque`) |
| Port | `8082` |
| Documentation | Swagger UI (`springdoc-openapi`) |
| Vérification | `curl`, Swagger UI, tests MockMvc |

---

## 2. Structure du projet

```
lab8Microservice/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/ma/rest/spring/
    │   │   ├── MsBanqueApplication.java          # @SpringBootApplication + CommandLineRunner
    │   │   ├── entities/
    │   │   │   ├── Compte.java                   # @Entity
    │   │   │   └── TypeCompte.java               # enum COURANT / EPARGNE
    │   │   ├── repositories/
    │   │   │   └── CompteRepository.java         # JpaRepository<Compte, Long>
    │   │   └── web/
    │   │       └── CompteController.java         # @RestController @RequestMapping("/banque")
    │   └── resources/
    │       └── application.properties            # datasource H2, port 8082, console H2
    └── test/java/ma/rest/spring/web/
        └── CompteControllerTest.java             # 5 tests MockMvc (JSON + XML)
```

---

## 3. Dépendances (pom.xml)

Projet basé sur **`spring-boot-starter-parent 3.2.5`** (Java 17).

| Dépendance | Rôle |
|---|---|
| `spring-boot-starter-web` | Spring MVC, Tomcat embarqué, `@RestController` |
| `spring-boot-starter-data-jpa` | `JpaRepository`, Hibernate 6 |
| `h2` (runtime) | Base de données en mémoire |
| `lombok` | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| `spring-boot-devtools` | Rechargement automatique en développement |
| `springdoc-openapi-starter-webmvc-ui 2.5.0` | Swagger UI + `/v3/api-docs` |
| `jackson-dataformat-xml` | Sérialisation / désérialisation XML |
| `spring-boot-starter-test` | JUnit 5, MockMvc, AssertJ |

---

## 4. Configuration — `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:banque
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8082
spring.jackson.date-format=yyyy-MM-dd
spring.jpa.show-sql=true
```

- `ddl-auto=update` : Hibernate crée / met à jour le schéma `compte` automatiquement au démarrage.
- Console H2 : <http://localhost:8082/h2-console> (URL JDBC `jdbc:h2:mem:banque`, user `sa`, mot de passe vide).

---

## 5. Principe de l'API REST

### 5.1 Entité JPA

```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "compte")
public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double solde;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    private TypeCompte type;   // COURANT | EPARGNE
}
```

`@Enumerated(EnumType.STRING)` stocke le type en clair dans la base (plus lisible que `ORDINAL`).
`@JacksonXmlRootElement` donne la racine `<compte>` en XML.

### 5.2 Dépôt JPA

```java
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> { }
```

`findAll()`, `findById()`, `save()`, `delete()` sont générés par Spring Data — aucune implémentation à écrire.

### 5.3 Contrôleur REST

`@RestController` = `@Controller` + `@ResponseBody` : chaque méthode renvoie un objet directement dans le
corps de la réponse HTTP. `@RequestMapping("/banque")` fixe le préfixe commun.

| Méthode | URL | `consumes` | `produces` | Résultat |
|---|---|---|---|---|
| `GET` | `/banque/comptes` | — | JSON / XML | liste des comptes |
| `GET` | `/banque/comptes/{id}` | — | JSON / XML | `200` + compte, ou `404` |
| `POST` | `/banque/comptes` | JSON / XML | JSON / XML | compte créé (`200`) |
| `PUT` | `/banque/comptes/{id}` | JSON / XML | JSON / XML | compte mis à jour, ou `404` |
| `DELETE` | `/banque/comptes/{id}` | — | — | `200` si supprimé, `404` sinon |

Annotations utilisées :

- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` : type HTTP géré par la méthode ;
- `@PathVariable` : lie l'argument à une variable de chemin (`{id}`) ;
- `@RequestBody` : lie l'argument au corps de la requête (JSON ou XML) ;
- `ResponseEntity<T>` : contrôle le **code de statut**, le **corps** et les **en-têtes** de la réponse.

Le format de la réponse dépend de l'en-tête **`Accept`** envoyé par le client : c'est la négociation de
contenu de Spring MVC (`produces` déclare les types supportés).

```java
@GetMapping(value = "/comptes", produces = {"application/json", "application/xml"})
public List<Compte> getAllComptes() {
    return compteRepository.findAll();
}

@GetMapping(value = "/comptes/{id}", produces = {"application/json", "application/xml"})
public ResponseEntity<Compte> getCompteById(@PathVariable Long id) {
    return compteRepository.findById(id)
            .map(compte -> ResponseEntity.ok().body(compte))   // 200 OK
            .orElse(ResponseEntity.notFound().build());        // 404 Not Found
}
```

### 5.4 Initialisation des données

`CommandLineRunner` insère trois comptes aléatoires au démarrage puis les affiche dans la console :
`CommandLineRunner` s'exécute juste après le démarrage du contexte Spring.

### 5.5 Swagger / OpenAPI

`springdoc-openapi` analyse automatiquement les contrôleurs et génère la documentation :

- Swagger UI : <http://localhost:8082/swagger-ui/index.html>
- Spécification brute : <http://localhost:8082/v3/api-docs>

---

## 6. Compilation et lancement

Maven n'étant pas sur le `PATH`, on utilise le Maven embarqué d'IntelliJ :

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" -B -f "C:\Users\DELL\Desktop\lab8Microservice\pom.xml" clean test
```

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" -B -f "C:\Users\DELL\Desktop\lab8Microservice\pom.xml" spring-boot:run
```

Avec un Maven installé dans le `PATH`, les commandes deviennent simplement :

```bash
mvn clean test
mvn spring-boot:run
```

Logs de démarrage observés :

```
Tomcat started on port 8082 (http) with context path ''
Started MsBanqueApplication in 3.135 seconds
----- Comptes initialises -----
Compte(id=1, solde=6968.635532336985, dateCreation=2026-10-23, type=EPARGNE)
Compte(id=2, solde=343.0046886765998, dateCreation=2026-10-23, type=COURANT)
Compte(id=3, solde=4472.346509763803, dateCreation=2026-10-23, type=EPARGNE)
-------------------------------
Hibernate: create table compte (id bigint generated by default as identity, date_creation date,
           solde float(53) not null, type varchar(255) check (type in ('COURANT','EPARGNE')), primary key (id))
```

---

## 7. Tests avec curl (résultats réellement obtenus)

### a) Liste des comptes en JSON

```bash
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/json"
```

```json
[{"id":2,"solde":343.0046886765998,"dateCreation":"2026-10-23","type":"COURANT"},
 {"id":3,"solde":4472.346509763803,"dateCreation":"2026-10-23","type":"EPARGNE"},
 {"id":4,"solde":2500.0,"dateCreation":"2024-12-15","type":"EPARGNE"}]
HTTP 200 application/json
```

### b) Liste des comptes en XML

```bash
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/xml"
```

```xml
<List><item><id>2</id><solde>343.0046886765998</solde><dateCreation>2026-10-23</dateCreation><type>COURANT</type></item>
<item><id>3</id><solde>4472.346509763803</solde><dateCreation>2026-10-23</dateCreation><type>EPARGNE</type></item>
<item><id>4</id><solde>2500.0</solde><dateCreation>2024-12-15</dateCreation><type>EPARGNE</type></item></List>
HTTP 200 application/xml
```

### c) Création d'un compte en JSON

```bash
curl -X POST "http://localhost:8082/banque/comptes" \
     -H "Content-Type: application/json" -H "Accept: application/json" \
     -d '{"solde": 1500.0, "dateCreation": "2024-11-01", "type": "COURANT"}'
```

```json
{"id":5,"solde":1500.0,"dateCreation":"2024-11-01","type":"COURANT"}
HTTP 200 application/json
```

### d) Création d'un compte en XML

```bash
curl -X POST "http://localhost:8082/banque/comptes" \
     -H "Content-Type: application/xml" -H "Accept: application/xml" \
     -d '<compte><solde>2500.0</solde><dateCreation>2024-12-15</dateCreation><type>EPARGNE</type></compte>'
```

```xml
<compte><id>6</id><solde>2500.0</solde><dateCreation>2024-12-15</dateCreation><type>EPARGNE</type></compte>
HTTP 200 application/xml
```

### e) Mise à jour en JSON

```bash
curl -X PUT "http://localhost:8082/banque/comptes/5" \
     -H "Content-Type: application/json" -H "Accept: application/json" \
     -d '{"solde": 2000.0, "dateCreation": "2024-11-01", "type": "EPARGNE"}'
```

```json
{"id":5,"solde":2000.0,"dateCreation":"2024-11-01","type":"EPARGNE"}
HTTP 200 application/json
```

### f) Mise à jour en XML

```bash
curl -X PUT "http://localhost:8082/banque/comptes/2" \
     -H "Content-Type: application/xml" -H "Accept: application/xml" \
     -d '<compte><solde>3200.0</solde><dateCreation>2024-12-20</dateCreation><type>COURANT</type></compte>'
```

```xml
<compte><id>2</id><solde>3200.0</solde><dateCreation>2024-12-20</dateCreation><type>COURANT</type></compte>
HTTP 200 application/xml
```

### g) Suppression

```bash
curl -X DELETE "http://localhost:8082/banque/comptes/6"
```

```
HTTP 200
```

### h) Cas d'erreur : identifiant inexistant

```bash
curl -X GET "http://localhost:8082/banque/comptes/99999" -H "Accept: application/json"
```

```
HTTP 404
```

### i) Swagger, OpenAPI et console H2

```
GET /v3/api-docs           -> HTTP 200 application/json
GET /swagger-ui/index.html -> HTTP 200 text/html
GET /h2-console            -> HTTP 302   (redirection vers la page de connexion H2)
```

Swagger UI liste bien les 5 opérations du `compte-controller` :
`GET /banque/comptes`, `POST /banque/comptes`, `GET|PUT|DELETE /banque/comptes/{id}`.

---

## 8. Tests automatisés

`CompteControllerTest` (JUnit 5 + `MockMvc`) vérifie :

| Test | Vérification |
|---|---|
| `getComptesEnJson` | `200`, `Content-Type: application/json`, tableau JSON, champ `type` présent |
| `getComptesEnXml` | `200`, `Content-Type: application/xml`, racine `<List>` + éléments `<item>` |
| `getCompteByIdEnXml` | `200`, racine `<compte>` (grâce à `@JacksonXmlRootElement`) |
| `createCompteEnJson` | `200`, `id` généré, `type` = `COURANT`, `dateCreation` = `2024-11-01` |
| `getCompteInexistantRetourne404` | `404 Not Found` |

```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 9. Écarts par rapport à l'énoncé (et pourquoi)

L'énoncé du TP contient quelques incohérences ; les adaptations suivantes ont été nécessaires :

| Point de l'énoncé | Adaptation | Raison |
|---|---|---|
| `javax.persistence.*` | `jakarta.persistence.*` | Spring Boot 3 / Hibernate 6 utilisent Jakarta EE 9+ ; `javax.persistence` n'existe plus. |
| `private Date dateCreation` + `@Temporal(DATE)` | `private LocalDate dateCreation` + `@JsonFormat` | Avec Hibernate 6, `java.util.Date` converti en `java.sql.Date` provoque un **décalage de fuseau horaire** à la relecture (`2024-12-15` devenait `2024-12-14`). Le contrat REST (`"yyyy-MM-dd"`) est inchangé. |
| `springdoc-openapi-starter-webmvc-ui:2.1.0` | version **2.5.0** | La 2.1.0 n'est pas alignée sur Spring Boot 3.2.5 ; la 2.5.0 est la version compatible. |
| Package `ma.rest.spring.entities`, imports `import import ...` | package `ma.rest.spring.{entities,repositories,web}` | Le `import import` répété dans l'énoncé est une coquille de frappe. |
| `CompteController` non localisé | placé dans `ma.rest.spring.web` | Le contrôleur est un composant web, il est séparé des entités et des dépôts. |
| Console H2 annoncée en `localhost:8080` | `localhost:8082` | Le port applicatif configuré est `8082` (le `8080` de l'énoncé est une coquille). |

Aucun autre ajout n'a été fait : pas de couche service, pas d'interface supplémentaire.
Les **5 tests** sont le seul code non demandé explicitement, ajoutés pour vérifier le support JSON/XML.

---

## 10. Points clés à retenir

- `@RestController` = `@Controller` + `@ResponseBody` → l'objet retourné est sérialisé dans le corps HTTP.
- `@RequestMapping("/banque")` définit le préfixe commun de tous les endpoints du contrôleur.
- `produces` / `consumes` déclarent les formats acceptés et retournés ; Spring choisit selon les en-têtes
  `Accept` et `Content-Type`.
- `ResponseEntity` permet de maîtriser le code HTTP (`ok`, `notFound`) et le corps renvoyé.
- `JpaRepository` supprime tout le code CRUD : `findAll`, `findById`, `save`, `delete`.
- `jackson-dataformat-xml` suffit à activer le XML ; `@JacksonXmlRootElement` fixe la racine du document.
- `springdoc-openapi` documente l'API sans annotation supplémentaire, avec une interface de test intégrée.
