# Production Deployment

Rain is intended to run as multiple stateless Java service replicas backed by a highly available MySQL 8 writer endpoint. No Redis or service registry is required.

## Runtime Model

- Segment mode stores allocation state in MySQL and uses atomic `LAST_INSERT_ID(max_id + step * segmentNum)` updates.
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
- Adjust NetworkPolicy egress for your MySQL writer endpoint.
- Keep `replicas <= 2^machineIdBits - 1`.
- Use PodAntiAffinity or topology spread constraints if your cluster spans zones.

## Database

Flyway migrations live in:

```text
rain-uidgenerator-server/src/main/resources/db/migration
```

The first migration creates:

- `mxsm_allocation`
- `mxsm_snowfalke_node`

For production, grant the application user only the permissions it needs for these tables and Flyway migration execution. The legacy [scripts/mxsm-uidgenerator.sql](../scripts/mxsm-uidgenerator.sql) is kept for local bootstrap and no longer drops databases or tables.

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
- MySQL connection pool saturation.
- HTTP 5xx rate above baseline.

## Rollout And Rollback

Use rolling deployment for normal backward-compatible releases. For risky changes, use canary traffic through your ingress/service mesh.

Rollback checklist:

- Previous image tag/digest is available.
- Flyway migration is backward-compatible.
- MySQL writer endpoint is healthy.
- Prometheus and logs are available during rollout.

## Verification

```bash
java -version
./mvnw -q -Dflatten.skip=true clean verify
./mvnw -q -Dflatten.skip=true -DskipTests -Prelease-rain-server install
docker build -t rain-uidgenerator:ci .
kubectl apply --dry-run=client --validate=false -f deploy/kubernetes/rain.yaml
```

Testcontainers MySQL integration tests run automatically when Docker is available.
