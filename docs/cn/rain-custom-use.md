# Java SDK 使用说明

## 远程模式

远程模式是默认模式。SDK 会调用服务端接口，支持多地址轮询、失败切换、超时、有限重试、Token 和优雅关闭。

```java
UidClient client = UidClient.builder()
    .setUidGeneratorServerUris("http://rain-uidgenerator:8080")
    .setToken(System.getenv("RAIN_UID_TOKEN"))
    .setConnectTimeout(Duration.ofSeconds(1))
    .setReadTimeout(Duration.ofSeconds(2))
    .setMaxRetries(2)
    .build();

try {
    long segmentId = client.getSegmentUid("orders");
    long snowflakeId = client.getSnowflakeUid();
} finally {
    client.shutdown();
}
```

`setUidGeneratorServerUir` 保留兼容但已废弃，新代码使用 `setUidGeneratorServerUris`。

## Segment 本地缓存模式

```java
UidClient client = UidClient.builder()
    .setUidGeneratorServerUris("http://rain-uidgenerator:8080")
    .setToken(System.getenv("RAIN_UID_TOKEN"))
    .isSegmentUidFromRemote(false)
    .setSegmentNum(32)
    .setThreshold(30)
    .setPrefetchThreads(2)
    .setSegmentWaitTimeoutMillis(3000)
    .build();
```

本地缓存模式在进程内生成 UID，号段仍由服务端通过 MySQL 原子分配，保证全局不重叠。当本地库存耗尽且补充超时时，会抛出 `UidUnavailableException`。

## Snowflake 本地模式

本地 Snowflake 不再使用随机 worker id，必须使用确定性配置。

显式配置：

```java
UidClient client = UidClient.builder()
    .isSnowflakeUidFromRemote(false)
    .setMachineId(7)
    .build();
```

Kubernetes StatefulSet：

```java
UidClient client = UidClient.builder()
    .isSnowflakeUidFromRemote(false)
    .isContainer(true)
    .setPodName(System.getenv("HOSTNAME"))
    .build();
```

`machineId` 必须在 `machineIdBits` 允许范围内，配置缺失或越界会快速失败。
