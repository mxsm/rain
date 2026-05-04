# Production Deployment

Rain is designed to run as multiple stateless service replicas backed by a highly available MySQL 8 writer endpoint.

## Runtime Requirements

- Java 25
- MySQL 8 / InnoDB
- Kubernetes for production Snowflake worker id stability
- Externalized secrets through environment variables or Kubernetes Secrets

## Required Environment

```bash
SPRING_PROFILES_ACTIVE=prod
RAIN_DATASOURCE_URL=jdbc:mysql://mysql-writer.example:3306/uidgenerator?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC
RAIN_DATASOURCE_USERNAME=rain
RAIN_DATASOURCE_PASSWORD=...
RAIN_UID_SECURITY_ENABLED=true
RAIN_UID_TOKENS=...
RAIN_UID_SNOWFLAKE_CONTAINER=true
```

For Kubernetes, deploy `deploy/kubernetes/rain.yaml` as a starting point. The `StatefulSet` pod ordinal is used as the Snowflake machine id, so keep the replica count within the configured machine-id bit range.

## APIs

All `/api/v1/**` success responses are wrapped:

```json
{
  "data": 123,
  "status": "SUCCESS",
  "code": "SUCCESS",
  "msg": "SUCCESS"
}
```

Errors use the same envelope with stable `code` values and HTTP status codes.

## Observability

Actuator endpoints:

- `/actuator/health/liveness`
- `/actuator/health/readiness`
- `/actuator/prometheus`

Custom metrics include segment generation, Snowflake generation, segment allocation latency/failures, clock rollback count, and worker id.

## Database

Flyway applies schema migrations from `rain-uidgenerator-server/src/main/resources/db/migration`. The legacy `scripts/mxsm-uidgenerator.sql` is safe for local bootstrap and does not drop existing databases or tables.
