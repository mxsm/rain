package com.github.mxsm.rain.uid.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class UidMetrics {

    private final Counter segmentGenerated;
    private final Counter snowflakeGenerated;
    private final Counter segmentAllocationFailure;
    private final Counter segmentDiscarded;
    private final Counter clockRollback;
    private final Timer segmentAllocationTimer;
    private final AtomicLong workerId = new AtomicLong(-1);

    public UidMetrics(MeterRegistry registry) {
        this.segmentGenerated = Counter.builder("rain_uid_segment_generated_total").register(registry);
        this.snowflakeGenerated = Counter.builder("rain_uid_snowflake_generated_total").register(registry);
        this.segmentAllocationFailure = Counter.builder("rain_uid_segment_allocation_failed_total").register(registry);
        this.segmentDiscarded = Counter.builder("rain_uid_segment_discarded_total").register(registry);
        this.clockRollback = Counter.builder("rain_uid_snowflake_clock_rollback_total").register(registry);
        this.segmentAllocationTimer = Timer.builder("rain_uid_segment_allocation_duration").register(registry);
        registry.gauge("rain_uid_snowflake_worker_id", workerId);
    }

    public void recordSegmentGenerated() {
        segmentGenerated.increment();
    }

    public void recordSnowflakeGenerated() {
        snowflakeGenerated.increment();
    }

    public void recordSegmentAllocation(long elapsedNanos) {
        segmentAllocationTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordSegmentAllocationFailure() {
        segmentAllocationFailure.increment();
    }

    public void recordSegmentDiscarded(long count) {
        if (count > 0) {
            segmentDiscarded.increment(count);
        }
    }

    public void recordClockRollback() {
        clockRollback.increment();
    }

    public void setWorkerId(long workerId) {
        this.workerId.set(workerId);
    }
}
