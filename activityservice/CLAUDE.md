# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`activityservice` is one Spring Boot module inside a larger `fitness-microservice` workspace
(`../userservice` is the sibling). Each service is an independent Maven project with its own
`pom.xml` and wrapper — there is no aggregator POM, so build and run each from its own directory.

| Service | Stack | Port | Datastore |
|---|---|---|---|
| activityservice (here) | Spring Boot 4.1.1 / Java 21 | 8082 | MongoDB `fitnessactivity` |
| userservice (`../userservice`) | Spring Boot / JPA | 8080 (default) | MySQL `fitness_db` |

The services do not call each other yet; `Activity.userId` is a plain string with no cross-service
validation.

## Commands

```bash
./mvnw spring-boot:run              # run on :8082 (needs Mongo reachable — see below)
./mvnw clean package                # build the fat jar into target/
./mvnw test                         # all tests
./mvnw test -Dtest=ActivityserviceApplicationTests            # one test class
./mvnw test -Dtest=ActivityserviceApplicationTests#contextLoads  # one test method
```

There is no linter or formatter configured.

## Running MongoDB

Mongo runs in a local Kubernetes cluster (Colima), not Docker Compose. `mongodb-setup/` holds the
PVC/Deployment/Service manifests and `mongodb-setup/Mongodb-setup.md` is the full walkthrough.
Short version:

```bash
kubectl create namespace mongodb
kubectl create secret generic mongo-secret -n mongodb \
  --from-literal=MONGO_INITDB_ROOT_USERNAME=root \
  --from-literal=MONGO_INITDB_ROOT_PASSWORD=rootpass
kubectl apply -f mongodb-setup/ -n mongodb
kubectl port-forward svc/mongodb-service 27017:27017 -n mongodb   # keep this terminal open
```

The app (and `ActivityserviceApplicationTests`, which is a `@SpringBootTest`) will not start
without that port-forward running.

## Architecture

Standard layered flow, one package per layer under `com.fsdarvind.fitness.activityservice`:

`ActivityController` → `ActivityService` → `ActivityRepository` (Spring Data `MongoRepository`) → `activities` collection

- DTOs (`ActivityRequest`/`ActivityResponse`) never leak the entity. `ActivityService.mapToResponse`
  is the single hand-written mapper — extend it when you add a field to `Activity`.
- `Activity.additionalMetrics` is a free-form `Map<String, Object>` persisted under the Mongo field
  name `metrics` (via `@Field`), which is why the DTO and document names differ.
- Lombok drives all boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`); `pom.xml` wires the
  annotation processor explicitly for both `default-compile` and `default-testCompile`.

Current API surface is a single endpoint: `POST /api/activities/` — note the trailing slash, the
`@PostMapping("/")` makes it significant.

## Gotchas

- **Spring Boot 4 property names.** MongoDB config lives under `spring.mongodb.*` in
  `application.yaml`, not the pre-Boot-4 `spring.data.mongodb.*`. `Mongodb-setup.md` still shows the
  old form — the yaml is correct, the doc is stale.
- **Auditing is not enabled.** `Activity` carries `@CreatedDate`/`@LastModifiedDate`, but no
  `@EnableMongoAuditing` exists anywhere, so `createdAt`/`updatedAt` persist and come back as `null`.
  Add `@EnableMongoAuditing` to `ActivityserviceApplication` if those fields are expected to populate.
- No bean validation, exception handling, or security layer exists yet — `ActivityRequest` fields are
  taken as-is, including a null `userId`.
- Mongo credentials are committed in plain text in `application.yaml` (local-only `root/rootpass`).
