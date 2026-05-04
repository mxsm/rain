# Rain 生产配置

Rain 当前基于 Java 25 和 Spring Boot 4。生产部署建议使用 Kubernetes StatefulSet + 外部 MySQL 8 高可用写端点。

## 必需环境变量

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

## Segment 配置

```bash
RAIN_UID_SEGMENT_CACHE_SIZE=32
RAIN_UID_SEGMENT_THRESHOLD=40
RAIN_UID_SEGMENT_PREFETCH_THREADS=4
RAIN_UID_SEGMENT_WAIT_TIMEOUT_MS=3000
```

说明：

- `cache-size`：本地缓存的号段数量。
- `threshold`：库存百分比低于阈值时触发预取。
- `prefetch-threads`：后台补充号段线程数。
- `wait-timeout-millis`：库存耗尽时等待补充号段的最长时间。

## Snowflake 配置

```bash
RAIN_UID_SNOWFLAKE_TIMESTAMP_BITS=41
RAIN_UID_SNOWFLAKE_MACHINE_ID_BITS=10
RAIN_UID_SNOWFLAKE_SEQUENCE_BITS=12
RAIN_UID_SNOWFLAKE_EPOCH=2022-05-01
RAIN_UID_SNOWFLAKE_MAX_BACKWARD_MILLIS=1000
```

生产 Kubernetes 模式默认从 StatefulSet pod 名称解析 ordinal 作为 worker id，例如 `rain-uidgenerator-2` 的 worker id 为 `2`。解析失败或超出 bit 范围时应用会失败，不会随机降级。

## 数据库

Flyway 迁移位于：

```text
rain-uidgenerator-server/src/main/resources/db/migration
```

号段分配使用 MySQL 原子更新：

```sql
UPDATE mxsm_allocation
SET max_id = LAST_INSERT_ID(max_id + step * ?)
WHERE biz_code = ?;
SELECT LAST_INSERT_ID();
```

该模式支持多实例并发分配且号段不重叠。

## 观测

健康检查：

- `/actuator/health/liveness`
- `/actuator/health/readiness`

Prometheus：

- `/actuator/prometheus`

重点指标：

- `rain_uid_segment_generated_total`
- `rain_uid_snowflake_generated_total`
- `rain_uid_segment_allocation_duration`
- `rain_uid_segment_allocation_failed_total`
- `rain_uid_segment_discarded_total`
- `rain_uid_snowflake_clock_rollback_total`
- `rain_uid_snowflake_worker_id`
