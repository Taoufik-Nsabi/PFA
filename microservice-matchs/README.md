# SIRMaroc - Microservice Matchs

Microservice Spring Boot de gestion des matchs pour la Coupe du Monde 2030 au Maroc.
Il fait partie de l'architecture microservices du projet SIRMaroc.

---

## Architecture

```text
microservice-matchs/
|-- src/main/java/ma/sirmaroc/matchs/
|   |-- MicroserviceMatchsApplication.java
|   |-- config/
|   |-- controller/
|   |-- dto/
|   |-- exception/
|   |-- integration/
|   |-- model/
|   |-- repository/
|   `-- service/
|-- src/main/resources/
|   |-- application.properties
|   |-- application-dev.properties
|   `-- application-prod.properties
|-- src/test/java/ma/sirmaroc/matchs/
|   `-- MatchServiceTest.java
`-- pom.xml
```

---

## Prerequis

- Java 21
- Maven 3.9+
- SQL Server 2019+ ou SQL Server Express

---

## Configuration

Le projet utilise maintenant:

- un fichier commun `application.properties`
- un profil `dev` pour le developpement local
- un profil `prod` pour une configuration plus stricte

### Profil `dev`

- `ddl-auto=validate`
- logs SQL actifs

### Profil `prod`

- `ddl-auto=validate`
- logs SQL limites
- confiance stricte envers l'API Gateway
- `GATEWAY_SHARED_SECRET` obligatoire cote infrastructure

### Variables d'environnement conseillees pour `prod`

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:sqlserver://host:1433;databaseName=sirmaroc_matchs;encrypt=true;trustServerCertificate=false
DB_USERNAME=your_user
DB_PASSWORD=your_password
GATEWAY_TRUST_ENABLED=true
GATEWAY_SHARED_SECRET=replace-with-a-long-random-secret
GATEWAY_HEADER_NAME=X-Gateway-Token
GATEWAY_USER_HEADER_NAME=X-Authenticated-User
GATEWAY_ROLES_HEADER_NAME=X-Authenticated-Roles
CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
```

### Variables d'environnement pour API-Football

```bash
API_FOOTBALL_ENABLED=true
API_FOOTBALL_BASE_URL=https://v3.football.api-sports.io
API_FOOTBALL_API_KEY=your_api_sports_key
API_FOOTBALL_TIMEZONE=Africa/Casablanca
```

Le projet contient maintenant un client minimal `ApiFootballClient` et des colonnes `api_football_id`
sur `Match`, `Equipe` et `Stade` pour faciliter les synchronisations et upserts depuis l'API externe.
Il expose aussi des endpoints d'administration pour synchroniser les equipes, stades et matchs
depuis API-Football vers SQL Server.

### Migration SQL Server

Le schema est maintenant gere par Flyway dans `src/main/resources/db/migration`.
Au demarrage de l'application, les migrations sont appliquees automatiquement avant Hibernate.

- une base vide est creee avec le schema courant
- une base plus ancienne est migree vers le nouveau modele quand c'est possible
- Hibernate est en mode `validate` pour verifier que le schema SQL Server correspond bien aux entites

Pour une base existante sans historique Flyway, `baseline-on-migrate=true` permet d'initialiser
la table d'historique puis d'appliquer les migrations incrementales suivantes.

### Verifier que SQL Server est joignable

En profil `dev`, vous pouvez verifier la connexion SQL Server avec:

```bash
GET http://localhost:8083/actuator/health
GET http://localhost:8083/actuator/health/db
```

Si la base est joignable, le composant `db` doit remonter `UP`.

### Base SQL Server locale

```sql
CREATE DATABASE sirmaroc_matchs;
GO
```

---

## Lancer le projet

### Developpement local

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Execution avec la configuration par defaut

```bash
mvn spring-boot:run
```

Le service demarre sur le port `8083` par defaut.

### Execution avec Docker

Construire l'image:

```bash
docker build -t sirmaroc/microservice-matchs .
```

Lancer le conteneur:

```bash
docker run --rm -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:sqlserver://host.docker.internal:1433;databaseName=sirmaroc_matchs;encrypt=true;trustServerCertificate=true" \
  -e DB_USERNAME=your_user \
  -e DB_PASSWORD=your_password \
  -e GATEWAY_TRUST_ENABLED=true \
  -e GATEWAY_SHARED_SECRET=replace-with-a-long-random-secret \
  -e GATEWAY_HEADER_NAME=X-Gateway-Token \
  -e GATEWAY_USER_HEADER_NAME=X-Authenticated-User \
  -e GATEWAY_ROLES_HEADER_NAME=X-Authenticated-Roles \
  -e API_FOOTBALL_ENABLED=true \
  -e API_FOOTBALL_API_KEY=your_api_sports_key \
  -e API_FOOTBALL_TIMEZONE=Africa/Casablanca \
  --name microservice-matchs \
  sirmaroc/microservice-matchs
```

Notes Docker:

- sur Windows et macOS, `host.docker.internal` permet au conteneur d'acceder a SQL Server qui tourne sur la machine hote
- si SQL Server tourne aussi dans Docker, remplacez `host.docker.internal` par le nom du service Docker
- le port expose par le conteneur reste `8083`

---

## API

### Stades - `/api/v1/stades`

- `GET /api/v1/stades`
- `GET /api/v1/stades/{id}`
- `GET /api/v1/stades/ville/{ville}`
- `POST /api/v1/stades`
- `PUT /api/v1/stades/{id}`
- `DELETE /api/v1/stades/{id}`

### Equipes - `/api/v1/equipes`

- `GET /api/v1/equipes`
- `GET /api/v1/equipes/{id}`
- `GET /api/v1/equipes/pays/{pays}`
- `POST /api/v1/equipes`
- `PUT /api/v1/equipes/{id}`
- `DELETE /api/v1/equipes/{id}`

### Matchs - `/api/v1/matchs`

- `GET /api/v1/matchs`
- `GET /api/v1/matchs/{id}`
- `GET /api/v1/matchs/ville/{ville}`
- `GET /api/v1/matchs/stade/{id}`
- `GET /api/v1/matchs/equipe/{id}`
- `GET /api/v1/matchs/date/{date}`
- `GET /api/v1/matchs/status/en-cours`
- `GET /api/v1/matchs/status/a-venir`
- `GET /api/v1/matchs/status/termines`
- `GET /api/v1/matchs/status/reportes`
- `GET /api/v1/matchs/status/suspendus`
- `POST /api/v1/matchs`
- `PUT /api/v1/matchs/{id}`
- `PATCH /api/v1/matchs/{id}/status`
- `DELETE /api/v1/matchs/{id}`

### Synchronisation API-Football - `/api/v1/sync/api-football`

- `POST /api/v1/sync/api-football/teams`
- `POST /api/v1/sync/api-football/fixtures`

Exemple pour synchroniser equipes et stades d'une ligue:

```json
{
  "leagueId": 39,
  "season": 2026
}
```

Exemple pour synchroniser les matchs d'une plage de dates:

```json
{
  "leagueId": 39,
  "season": 2026,
  "fromDate": "2026-08-01",
  "toDate": "2026-08-31",
  "syncTeamsBeforeFixtures": true
}
```

--- 

## Securite et API Gateway

Le microservice ne valide plus directement les JWT utilisateurs.
Il fait confiance a l'API Gateway, qui:

- valide le JWT recu du frontend
- bloque les requetes non authentifiees
- transmet l'identite et les roles au microservice

Le microservice attend maintenant les headers internes suivants pour toute requete `/api/v1/**`:

- `X-Gateway-Token`: secret partage entre le gateway et le microservice
- `X-Authenticated-User`: identifiant utilisateur deja valide par le gateway
- `X-Authenticated-Roles`: roles separes par des virgules, par exemple `ADMIN` ou `USER,ADMIN`

Comportement:

- toute requete directe vers `/api/v1/**` sans header gateway valide est refusee
- les `GET` restent fonctionnellement publics, mais uniquement lorsqu'ils transitent par le gateway
- les operations d'ecriture demandent le role `ADMIN` transmis par le gateway
- les endpoints de synchronisation API-Football demandent aussi le role `ADMIN`

---

## Documentation API

Swagger UI:

- [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## Tests

```bash
mvn test
```

---

## Integration SIRMaroc

```text
Frontend (React)
      |
   API Gateway
      |
  +---------+
  | Matchs  | <- ce microservice (8083)
  | Auth    | <- ASP.NET Core (8080)
  | Heberg  | <- Symfony PHP (8081)
  | Transp  | <- Spring Boot (8084)
  +---------+
```
