# Production Deployment

Point all Rain replicas in a deployment at the same MySQL 8 or PostgreSQL 16 writer.

## Runtime Model

- Segment mode advances one row per business code. MySQL locks the row and reads the new cursor with `LAST_INSERT_ID()` on the same connection. PostgreSQL returns the cursor and step from one `UPDATE ... RETURNING`.
- Snowflake mode uses a deterministic worker ID. In Kubernetes production deployment, the StatefulSet pod ordinal is the worker ID.
- Service replicas can scale horizontally as long as the replica count is within the configured `machine-id-bits` range.

## Required Environment

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

Useful tuning variables:

```bash
RAIN_DATASOURCE_MAX_POOL_SIZE=20
RAIN_DATASOURCE_MIN_IDLE=4
RAIN_DATASOURCE_CONNECTION_TIMEOUT_MS=3000
RAIN_UID_SEGMENT_CACHE_SIZE=32
RAIN_UID_SEGMENT_THRESHOLD=40
RAIN_UID_SEGMENT_PREFETCH_THREADS=4
RAIN_UID_SEGMENT_WAIT_TIMEOUT_MS=3000
RAIN_UID_SNOWFLAKE_MAX_BACKWARD_MILLIS=1000
```

## Security

Enable token authentication in production:

- `RAIN_UID_TOKENS`: generation/read tokens.
- `RAIN_UID_ADMIN_TOKENS`: admin tokens. Required for `POST /api/v1/segment/rg`.

Do not store real secrets in Kubernetes manifests. Use External Secrets, Sealed Secrets, your cloud secret manager, or manually created Kubernetes Secrets.

## Kubernetes

Start from [deploy/kubernetes/rain.yaml](../deploy/kubernetes/rain.yaml).

The manifest includes:

- Namespace
- ConfigMap
- Secret placeholder
- Headless Service for StatefulSet identity
- ClusterIP Service for normal clients
- StatefulSet
- readiness/liveness/startup probes
- PodDisruptionBudget
- HorizontalPodAutoscaler
- NetworkPolicy

Important production adjustments:

- Replace `ghcr.io/mxsm/rain:1.0.1` with your immutable image tag or digest.
- Replace `change-me` secret placeholders.
- Scope NetworkPolicy egress to your writer endpoint and its port (3306 for MySQL or 5432 for PostgreSQL).
- Keep `replicas <= 2^machineIdBits - 1`.
- Use PodAntiAffinity or topology spread constraints if your cluster spans zones.

## Database

Flyway migrations live in:

```text
rain-uidgenerator-server/src/main/resources/db/migration/{mysql,postgresql}
```

The first migration creates:

- `mxsm_allocation`
- `mxsm_snowfalke_node`

For PostgreSQL, use a URL such as `jdbc:postgresql://pg-writer.example:5432/uidgenerator?sslmode=verify-full`. Flyway needs permission to create tables and an ICU collation in the target schema. Business keys ignore case, accents, and trailing ASCII spaces. When importing Unicode keys from MySQL, check their uniqueness under PostgreSQL's ICU collation. Its Unicode weights can differ from `utf8mb4_general_ci`.

MySQL databases with a recorded Flyway V1 upgrade in place. For databases initialized manually with the legacy bootstrap script, verify both table definitions against MySQL V1. On the first startup set `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` and `SPRING_FLYWAY_BASELINE_VERSION=0` so Flyway records the existing idempotent V1. Remove the overrides after startup. [MySQL bootstrap script](../scripts/mxsm-uidgenerator.sql)

For a MySQL-to-PostgreSQL cutover, stop allocation on the old writer and drain the old replicas. Copy each business code, step and stored `max_id` (the exclusive upper bound of all reserved segments, including client caches). Copy the Snowflake node IDs and advance both identity sequences past the imported IDs before starting Rain against PostgreSQL. After issuing new ranges, a rollback must carry the latest cursors back to the old writer.

## API Compatibility

UID generation should use POST:

- `POST /api/v1/segment/uid/{bizCode}`
- `POST /api/v1/snowflake/uid`

Legacy GET endpoints remain available for compatibility but are deprecated. All `/api/v1/**` responses include `Cache-Control: no-store`.

## Observability

Actuator endpoints:

- `/actuator/health/liveness`
- `/actuator/health/readiness`
- `/actuator/prometheus`

Custom metrics:

- `rain_uid_segment_generated_total`
- `rain_uid_snowflake_generated_total`
- `rain_uid_segment_allocation_duration`
- `rain_uid_segment_allocation_failed_total`
- `rain_uid_segment_discarded_total`
- `rain_uid_snowflake_clock_rollback_total`
- `rain_uid_snowflake_worker_id`

Recommended alerts:

- Readiness failures > 0 for more than 2 minutes.
- Segment allocation failures > 0.
- Segment discarded count > 0.
- Clock rollback count > 0.
- Database connection pool saturation.
- HTTP 5xx rate above baseline.

## Rollout And Rollback

Use rolling deployment for normal backward-compatible releases. For risky changes, use canary traffic through your ingress/service mesh.

Rollback checklist:

- Previous image tag/digest is available.
- Flyway migration is backward-compatible.
- The configured database writer is healthy.
- Prometheus and logs are available during rollout.

## Verification

```bash
java -version
./mvnw -q -Dflatten.skip=true clean verify
./mvnw -q -Dflatten.skip=true -DskipTests -Prelease-rain-server install
docker build -t rain-uidgenerator:ci .
kubectl apply --dry-run=client --validate=false -f deploy/kubernetes/rain.yaml
```

Testcontainers runs the MySQL 8.4 and PostgreSQL 16 integration suites when Docker is available.
