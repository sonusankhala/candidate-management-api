# AI Usage Document — CandidateHub

## Where AI Was Used

AI (Claude) was used throughout: initial stack/architecture decisions, generating boilerplate (entity, DTOs, mapper, repository, service, controller, exception handling), the dynamic filtering logic and the test suite.

## How Output Was Validated

Every piece of code was compiled, run, and manually tested via Postman before being accepted. The automated test suite was written to independently verify the generated business logic. Several bugs below were only found by actually running the code and reading the real error output — not by inspection.

## Bugs Found and Fixed

1. **springdoc-openapi version mismatch** — the suggested version (2.6.0) targeted Spring Boot 3.x; this project runs Spring Boot 4.1.2, and it failed at runtime (500 on `/v3/api-docs`). Searched for the correct Spring Boot 4-compatible version (3.2.0) and verified Swagger UI loaded.

2. **`@DataJpaTest` import path** — the Spring Boot 3.x import path doesn't exist in Spring Boot 4; the annotation moved to a new module (`spring-boot-data-jpa-test`). Found the new path and dependency via Spring Boot 4's actual docs.

3. **MySQL-only SQL syntax breaking H2 tests** — `UNIQUE KEY name (column)` is valid MySQL but not standard SQL; H2 rejected it even in MySQL-compatibility mode. Root-caused via the Flyway/H2 stack trace and rewrote it as `CONSTRAINT ... UNIQUE (...)`, which both databases accept.

4. **H2's stricter LIKE ESCAPE handling** — `\` as the escape character worked on MySQL but H2 rejected it. Switched to `!` in both the query-building code and the matching escape helper.

5. **A test asserting against the wrong field** — a wildcard-escaping test was written against `location` (exact-match filter) instead of `q` (the actual LIKE-based filter), so it wasn't testing what it claimed to. Caught by tracing the actual failure, not just rerunning until green; corrected to target the right field.

6. **Spring Boot 4's Jackson/MockMvc test wiring** — `@Autowired ObjectMapper` failed because Spring Boot 4 doesn't reliably register it as a bean the same way Boot 3 did. Fixed by instantiating `ObjectMapper` directly in the test, and corrected `@AutoConfigureMockMvc`'s import path and the required test-starter dependency to match Spring Boot 4's modularized test infrastructure.

## Where AI Recommendations Were Not Followed

- **Dynamic filtering approach** — a hardcoded-query-method-per-filter-combination approach was considered and rejected in favor of JPA Specifications: every filter is independently optional and composed as a predicate only when present, so adding a new filter later means adding one predicate, not a new method.
- **MapStruct** for entity/DTO mapping — used a hand-written mapper instead, since the entity is small (~17 fields) and manual mapping keeps every conversion step visible and easy to explain line by line, rather than relying on annotation-processor-generated code.
- **Lombok `@Data`** on the JPA entity — used only `@Getter`/`@Setter`/`@NoArgsConstructor`, since `@Data`'s generated `equals()`/`hashCode()` can cause issues with lazy-loaded JPA collections and proxy objects.
- **Strict full-replace PUT** — initial behavior cleared any field omitted from a request. Changed to partial-update semantics after testing showed it silently wiped fields the client didn't intend to touch; noted in DESIGN.md as a trade-off, with a proper PUT/PATCH split as a follow-up.
- **Microservices** — considered as a discussion point early on, rejected for implementation — this domain has no independent scaling/deployment need that justifies the added complexity at this scope.
- **UUID primary keys** — considered for the `candidates` table, but `BIGINT AUTO_INCREMENT` was used instead; UUIDs mainly pay off for distributed ID generation across multiple services, which doesn't apply to a single monolithic service.
- **Database-generated `ON UPDATE CURRENT_TIMESTAMP` for `updated_at`** — removed from the schema in favor of relying solely on Hibernate's `@UpdateTimestamp`, so the timestamp's source of truth is one place (the application layer) rather than split between the database and the ORM.
- **Branch-per-feature / UAT-branch workflow** — considered for this project, but decided against it in favor of working directly on `main` with small, incremental commits — branching strategies like UAT environments and cherry-picking solve team-coordination problems that don't exist for a single-developer take-home; automated tests were used as the actual verification gate instead.
