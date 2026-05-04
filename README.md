# Rain

Rain is a Java 25 / Spring Boot 4 distributed UID generation service. It supports two generation modes:

- Segment IDs backed by MySQL atomic allocation.
- Snowflake IDs with deterministic worker IDs.

Production guidance is in [docs/production.md](docs/production.md). API details are in [docs/api.md](docs/api.md). Java SDK usage is in [docs/sdk.md](docs/sdk.md). Chinese docs are under [docs/cn](docs/cn).

## Requirements

- JDK 25
- Maven 3.9.15, preferably through `./mvnw`
- MySQL 8 / InnoDB
- Docker and Kubernetes for production delivery checks

## Build And Test

```bash
java -version
./mvnw -q -Dflatten.skip=true clean verify
./mvnw -q -Dflatten.skip=true -DskipTests -Prelease-rain-server install
```

`-Dflatten.skip=true` is recommended for local verification so exploratory Maven commands do not rewrite POM files.

## Local Run

Create the schema through Flyway by starting the server with a MySQL database available:

```bash
export SPRING_PROFILES_ACTIVE=local
export RAIN_DATASOURCE_URL='jdbc:mysql://localhost:3306/uidgenerator?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export RAIN_DATASOURCE_USERNAME=rain
export RAIN_DATASOURCE_PASSWORD=rain

./mvnw -q -Dflatten.skip=true -DskipTests -pl rain-uidgenerator-server -am package
java -jar rain-uidgenerator-server/target/rain-uidgenerator-server-1.0.1.jar
```

Register a segment business code:

```bash
curl -X POST http://localhost:8080/api/v1/segment/rg \
  -H 'Content-Type: application/json' \
  -d '{"bizCode":"orders","step":1000}'
```

Generate IDs:

```bash
curl -X POST http://localhost:8080/api/v1/snowflake/uid
curl -X POST http://localhost:8080/api/v1/segment/uid/orders
```

All `/api/v1/**` responses are wrapped in `Result<T>` with stable `code` values. UID generation uses POST. Legacy GET generation endpoints remain available for compatibility and return `Cache-Control: no-store`.

## Java SDK

```java
UidClient client = UidClient.builder()
    .setUidGeneratorServerUris("http://rain-uidgenerator:8080")
    .setToken(System.getenv("RAIN_UID_TOKEN"))
    .setConnectTimeout(Duration.ofSeconds(1))
    .setReadTimeout(Duration.ofSeconds(2))
    .setMaxRetries(2)
    .build();

long id = client.getSegmentUid("orders");
```

For local Snowflake mode the SDK no longer uses random worker IDs. Configure a stable `machineId` or Kubernetes pod ordinal:

```java
UidClient local = UidClient.builder()
    .isSnowflakeUidFromRemote(false)
    .setMachineId(7)
    .build();
```

## Production

Production mode expects externalized configuration:

```bash
SPRING_PROFILES_ACTIVE=prod
RAIN_DATASOURCE_URL=jdbc:mysql://mysql-writer.example:3306/uidgenerator?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC
RAIN_DATASOURCE_USERNAME=rain
RAIN_DATASOURCE_PASSWORD=...
RAIN_UID_SECURITY_ENABLED=true
RAIN_UID_TOKENS=...
RAIN_UID_ADMIN_TOKENS=...
RAIN_UID_SNOWFLAKE_CONTAINER=true
```

Kubernetes manifests are provided under [deploy/kubernetes/rain.yaml](deploy/kubernetes/rain.yaml). The StatefulSet ordinal is used as the Snowflake worker ID in production container mode.

## Observability

- `/actuator/health/liveness`
- `/actuator/health/readiness`
- `/actuator/prometheus`

Custom metrics include UID generation counters, segment allocation latency/failures, discarded segment count, clock rollback count, and worker ID.
