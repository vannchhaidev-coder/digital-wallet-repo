# my-platform

Monorepo for the auth + gateway microservice stack.

## Structure
```
my-platform/
├── api-gateway/     — public entry point, validates JWTs, routes to services
├── auth-service/     — owns user credentials, issues/refreshes JWTs
├── docker-compose.yml
└── README.md
```

## Build tool and dependency management
This is one Gradle **multi-project build**, not two separate Gradle projects that happen
to sit in the same folder. The root `settings.gradle` declares both modules:
```groovy
include 'auth-service'
include 'api-gateway'
```

**Dependency versions live in exactly one place**: `gradle/libs.versions.toml`, the Gradle
version catalog. Every service's `build.gradle` references versions by name (e.g.
`libs.jjwt.api`) instead of hardcoding a version string — bump `jjwt = "0.12.6"` once in
the catalog and both services pick it up on the next build. Add a new shared library the
same way: define it under `[libraries]` in the catalog, then reference it as
`libs.<name>` from whichever module needs it.

Run everything from the repo root, not from inside a service folder:
```
gradle build                      # builds every module
gradle :auth-service:build        # just one module
gradle :auth-service:bootRun      # run one service directly, no Docker
```

If you want a committed `./gradlew` wrapper instead of relying on a system Gradle
install, run this once from the repo root (requires Gradle installed locally just for
this one command):
```
gradle wrapper --gradle-version 8.10
```
That generates `gradlew`, `gradlew.bat`, and `gradle/wrapper/` at the root — commit them,
then use `./gradlew build` / `./gradlew :auth-service:bootRun` from then on.

**Docker build context note**: because both services now share root-level
`settings.gradle` and `gradle/libs.versions.toml`, each `Dockerfile` builds with the repo
root as its context (see `docker-compose.yml`'s `context: . / dockerfile: <service>/Dockerfile`),
not the service folder alone — Gradle needs those root files to resolve the module at all.

## Running locally
1. Generate a JWT secret once: run `JwtKeyGenerator.main()` inside auth-service.
2. Export it: `export JWT_SECRET=<generated base64 value>`
3. From the repo root: `docker compose up --build`
4. Gateway is at `http://localhost:8080` — this is the ONLY port you should call directly.
   `auth-service` (8081) is reachable inside the docker network but not meant to be exposed
   publicly once this moves past local dev.

## Request flow
```
client → api-gateway (:8080) → validates JWT → forwards to auth-service / order-service
```
- `api-gateway` strips any client-supplied `X-User-Id`/`X-User-Role` headers on every
  request, then sets its own from the validated token. Downstream services trust those
  headers — that trust is only valid as long as they're unreachable except through the
  gateway.
- Public routes (no token required): `/api/v1/auth/register`, `/api/v1/auth/login`,
  `/api/v1/auth/refresh`.
- Everything else requires `Authorization: Bearer <token>`.

## Adding a new service
1. Add the folder + its own `pom.xml`/Dockerfile.
2. Add a route in `api-gateway/.../config/RouteConfig.java`.
3. Add it to `docker-compose.yml`.
4. If any of its endpoints should be public, add them to `PUBLIC_PATHS` in
   `JwtAuthenticationGlobalFilter`.
