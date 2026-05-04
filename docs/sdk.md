# Java SDK

Dependency:

```xml
<dependency>
  <groupId>com.github.mxsm</groupId>
  <artifactId>rain-uidgenerator-client</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Remote Mode

Remote mode is the default. The SDK calls Rain server APIs and supports endpoint round-robin, failure failover, timeouts, finite retries, token headers, and graceful shutdown.

```java
import com.github.mxsm.rain.uid.client.UidClient;
import java.time.Duration;

UidClient client = UidClient.builder()
    .setUidGeneratorServerUris(
        "http://rain-uidgenerator-0.rain-uidgenerator-headless:8080",
        "http://rain-uidgenerator-1.rain-uidgenerator-headless:8080",
        "http://rain-uidgenerator-2.rain-uidgenerator-headless:8080")
    .setToken(System.getenv("RAIN_UID_TOKEN"))
    .setConnectTimeout(Duration.ofSeconds(1))
    .setReadTimeout(Duration.ofSeconds(2))
    .setMaxRetries(2)
    .setRetryBackoff(Duration.ofMillis(50))
    .setFailurePenalty(Duration.ofSeconds(1))
    .build();

try {
    long segmentId = client.getSegmentUid("orders");
    long snowflakeId = client.getSnowflakeUid();
} finally {
    client.shutdown();
}
```

`setUidGeneratorServerUir` is kept for compatibility but is deprecated. Use `setUidGeneratorServerUris`.

## Local Segment Cache

Local segment mode fetches batches from the server and generates IDs in-process:

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

This mode reduces request latency while MySQL allocation still remains globally atomic. If all local segments are exhausted and refill does not arrive before `segmentWaitTimeoutMillis`, the SDK throws `UidUnavailableException`.

## Local Snowflake Mode

Local Snowflake mode no longer uses random worker IDs. Configure one deterministic source.

Explicit machine ID:

```java
UidClient client = UidClient.builder()
    .isSnowflakeUidFromRemote(false)
    .setMachineId(7)
    .setMaxBackwardMillis(1000)
    .build();
```

Kubernetes StatefulSet pod ordinal:

```java
UidClient client = UidClient.builder()
    .isSnowflakeUidFromRemote(false)
    .isContainer(true)
    .setPodName(System.getenv("HOSTNAME"))
    .build();
```

The worker ID must be within the configured `machineIdBits` range. Invalid or missing worker IDs fail fast.

## Error Handling

Server-side failures are converted to `UidGenerateException` or `UidUnavailableException`. Remote server error envelopes are parsed with Jackson, not fastjson.
