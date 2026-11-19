# Rain 生产配置

Rain 基于 Java 25 和 Spring Boot 4。生产部署可使用 Kubernetes StatefulSet，所有副本连接同一个 MySQL 8 或 PostgreSQL 16 写端点。

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

Flyway 根据 JDBC URL 选择 `mysql/` 或 `postgresql/` 下的迁移。MySQL 在同一事务中锁定业务行，更新游标并读取结果。

```sql
UPDATE mxsm_allocation
SET max_id = LAST_INSERT_ID(max_id + step * ?)
WHERE id = ?;
SELECT LAST_INSERT_ID();
```

PostgreSQL 16 需要 ICU 支持。将 `RAIN_DATASOURCE_URL` 设为 `jdbc:postgresql://pg-writer.example:5432/uidgenerator?sslmode=verify-full`，并允许访问写库的 5432/TCP。服务通过一次 `UPDATE ... RETURNING` 取得本次分配的步长和游标。两个数据库均按业务码串行预留号段。

存量切库时先停止旧库分配，复制每个业务码的 `max_id`、`step` 和 Snowflake 节点 ID，再校准新库的自增序列。`max_id` 包含服务端和 SDK 已预取的号段。导入前须用目标库的 ICU 排序规则核对业务码唯一性。[生产部署说明](../production.md)

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
