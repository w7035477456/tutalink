# tutalink — Link Shortener Microservice

Java Spring Boot microservice that shortens marketing URLs for TutaMall (`https://tutamall.com/l/{code}`), records clicks, and exposes stats. Runs **side by side** with TutaMall on the same hostname (no CORS): paths `/l/**` and `/tutalink/**` go to this service on port **4100**.

## Stack

- Java 21 · Spring Boot 4 · Maven Wrapper
- Postgres schema `tutalink` (Primary writes / Replica reads)
- Redis cache + rate limits (`tutalink:` key prefix)
- Flyway · Spring Security (API key) · Actuator · springdoc OpenAPI
- PM2 on Ubuntu web hosts selected by `DEPLOY_ARCHITECTURE` (ALL-IN-ONE → xbox0; MINI_PRODUCTION → xbox2+xbox3)

## Quick start (Mac)

```bash
# 1) Schema + user (once, as test_user1)
psql -h 127.0.0.1 -p 50010 -U test_user1 -d outdatedDBOct2021 -f deploy/sql/000_create_schema_and_user.sql
# then: ALTER ROLE tutalink_user PASSWORD '...';

# 2) Secrets
mkdir -p ~/.ssh/tutalink && cp .env.example ~/.ssh/tutalink/.env
# edit DB_PASSWORD, TUTALINK_API_KEY, TUTALINK_IP_SALT, TUTALINK_LOG_DIR

# 3) Build & test
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home)
./mvnw verify

# 4) Run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Smoke curls

```bash
export KEY=your-api-key

curl -s -X POST http://localhost:4100/tutalink/api/links \
  -H "X-API-Key: $KEY" -H 'Content-Type: application/json' \
  -d '{"url":"https://tutamall.com/mall","customCode":"fb3"}'

curl -sI http://localhost:4100/l/fb3
curl -s http://localhost:4100/tutalink/api/links/fb3/stats -H "X-API-Key: $KEY"
curl -s http://localhost:4100/tutalink/actuator/health
```

OpenAPI: `http://localhost:4100/tutalink/api/docs`

## API (summary)

| Method | Path | Auth | Behavior |
|--------|------|------|----------|
| POST | `/tutalink/api/links` | X-API-Key | Create (30/min rate limit) |
| GET | `/tutalink/api/links` | key | Paginated list |
| GET | `/tutalink/api/links/{code}` | key | Details |
| GET | `/tutalink/api/links/{code}/stats` | key | Click stats |
| PATCH | `/tutalink/api/links/{code}` | key | Activate / expiry |
| DELETE | `/tutalink/api/links/{code}` | key | Delete |
| GET | `/l/{code}` | public | 302 redirect |
| GET | `/tutalink/actuator/health` | public | DB + Redis |

## Deploy architecture

Reads `DEPLOY_ARCHITECTURE` from `~/.ssh/be/.env` (same as TutaMall), optional override in `~/.ssh/tutalink/.env`:

| Value | Primary | Web (tutalink :4100) |
|-------|---------|----------------------|
| `ALL-IN-ONE` | xbox0 | xbox0 (Mac: run locally with `dev` profile) |
| `MINI_PRODUCTION` | xbox1 (R830) | xbox2 (R730) + xbox3 (R630) |
| `FULL_PRODUCTION` | — | not implemented yet |

```bash
./scripts/print-architecture.sh   # show resolved hosts
# from Mac (VPN + SSH hardened path)
./scripts/deploy.sh               # rolling deploy to architecture web hosts
./scripts/health-all.sh
./scripts/compare-schema.sh       # Mac vs Primary; prints same | difference
```

PM2 process name: `tutalinkPM2Process` (`ecosystem.config.js`).

## Suggested aliases (`~/b`)

```bash
alias tlrun='cd ~/code/tutalink && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev'
alias tltest='cd ~/code/tutalink && ./mvnw verify'
alias tldeploy='~/code/tutalink/scripts/deploy.sh'
alias tlhealth='~/code/tutalink/scripts/health-all.sh'
```

## Interview Notes — Spring concepts in this repo

| Concept | Where |
|---------|--------|
| `@RestController` vs `@Controller` | `LinkController` |
| `@Service` / `@Repository` / `@Component` / `@Configuration` / `@Bean` | service/, repository/, mapper/, config/ |
| Constructor injection | all services/controllers — no field `@Autowired` |
| `@Transactional` / `readOnly` + proxy / self-invocation | `LinkServiceImpl` |
| Primary/Replica routing | `DataSourceConfig` (`AbstractRoutingDataSource` + `LazyConnectionDataSourceProxy`) |
| Derived query / JPQL / native / projection / Pageable | `LinkRepository`, `ClickRepository` |
| N+1 + `@EntityGraph` + `open-in-view=false` | `LinkRepository` comments |
| `@Valid` / `@NotBlank` / `@URL` / `@Future` / `@Pattern` | `CreateLinkRequest` |
| `@RestControllerAdvice` + `ProblemDetail` | `GlobalExceptionHandler` |
| `@Cacheable` / `@CacheEvict` + Redis TTL | `LinkServiceImpl`, `RedisCacheConfig` |
| `@Async` + named executor | `ClickService`, `AsyncConfig` |
| SecurityFilterChain STATELESS + `OncePerRequestFilter` | `SecurityConfig`, `ApiKeyFilter` |
| `@ConfigurationProperties` record | `TutalinkProperties` |
| Profiles `dev` / `prod` / `test` | `application-*.yml` |

## Layout

See `src/main/java/com/tutamall/tutalink/` — controllers, services, repositories, entities, config, security, web filters. Cluster rules live in `.cursor/rules/tutalink.mdc`.
