# CandidateHub — Candidate Management API

A REST API for managing candidate information, built with Java 21 and Spring Boot 4. Supports full CRUD, free-text search, and multi-criteria filtering over candidates and their skills.

## Tech Stack

- **Java 21**, **Spring Boot 4.1**
- **MySQL 8** (via Docker Compose) for persistence
- **Spring Data JPA / Hibernate** for data access
- **Flyway** for database migrations
- **JPA Specifications** for dynamic, multi-criteria search
- **springdoc-openapi (Swagger UI)** for interactive API docs
- **JUnit 5, Mockito, AssertJ, H2, MockMvc** for automated testing

## Prerequisites

- Java 21 (JDK)
- Docker Desktop (for MySQL)
- Git

No local Maven install is required — the project includes the Maven Wrapper (`./mvnw`).

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/sonusankhala/candidate-management-api.git
   cd candidate-management-api
   ```

2. **Start MySQL** (via Docker Compose)
   ```bash
   docker compose up -d
   docker compose ps   # wait until status shows "healthy"
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   The app starts on `http://localhost:8080`. Flyway automatically applies the database schema on startup.

4. **Verify it's running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Expected: `{"status":"UP"}`

## API Documentation

Interactive Swagger UI (try endpoints directly from the browser): http://localhost:8080/swagger-ui.html


## Running Tests

```bash
./mvnw test
```

This runs the full automated test suite (27 tests) across three layers:
- **Unit tests** (`CandidateServiceTest`) — business logic, mocked dependencies
- **Repository tests** (`CandidateSpecificationsTest`) — dynamic filtering against an in-memory H2 database
- **Integration tests** (`CandidateControllerIntegrationTest`) — full HTTP request/response cycle via MockMvc

Tests run against H2 (in-memory) and require no Docker/MySQL to execute.

## API Overview

Base path: `/api/v1/candidates`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/v1/candidates` | Create a candidate |
| GET    | `/api/v1/candidates/{id}` | Get a candidate by id |
| GET    | `/api/v1/candidates` | Search/filter candidates (paginated) |
| PUT    | `/api/v1/candidates/{id}` | Update a candidate |
| DELETE | `/api/v1/candidates/{id}` | Delete a candidate |

### Search/Filter Query Parameters

All optional, combinable in any combination:

| Parameter | Type | Description |
|---|---|---|
| `q` | string | Free-text search across name, email, headline, company |
| `skills` | comma-separated | Filter by skill(s) |
| `matchAllSkills` | boolean | `true` = must have all listed skills, `false` (default) = any |
| `location` | string | Exact match on location |
| `status` | enum | `NEW`, `SCREENING`, `INTERVIEWING`, `OFFERED`, `HIRED`, `REJECTED` |
| `minExperience` / `maxExperience` | integer | Experience range in years |
| `currentCompany` | string | Exact match on current employer |
| `maxNoticePeriod` | integer | Max notice period in days |
| `source` | string | How the candidate was sourced |
| `page`, `size`, `sort` | — | Pagination (`size` capped at 100) and sorting (e.g. `sort=experienceYears,asc`) |

### Example: Create a candidate

```bash
curl -X POST http://localhost:8080/api/v1/candidates \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test,
    "lastName": "User",
    "email": "test.user@example.com",
    "location": "Hyderabad",
    "experienceYears": 4,
    "status": "NEW",
    "skills": ["java", "spring boot"]
  }'
```

### Example: Search

```bash
curl "http://localhost:8080/api/v1/candidates?location=Hyderabad&minExperience=3&skills=java&matchAllSkills=false"
```

## Project Structure
src/main/java/com/sonu/candidatehub/
domain/ Candidate entity, CandidateStatus enum
dto/ Request/response/filter DTOs
mapper/ Entity ↔ DTO conversion
repository/ Spring Data repository + dynamic Specifications
service/ Business logic
controller/ REST endpoints
exception/ Custom exceptions + global exception handler
config/ OpenAPI configuration
src/main/resources/
application.yml
db/migration/ Flyway SQL migrations
src/test/java/ Unit, repository, and integration tests

## Further Documentation

- [DESIGN.md](./DESIGN.md) — architecture decisions, data model, and scaling strategy
- [AI_USAGE.md](./AI_USAGE.md) — how AI tools were used during development
