package com.github.mxsm.rain.jmh;

import com.github.mxsm.rain.uid.client.Config;
import com.github.mxsm.rain.uid.client.service.SegmentUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.core.segment.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 4)
@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SegmentClientCachedBenchmark {

    private static final String BIZ_CODE = "benchmark";

    private SegmentUidGeneratorClientImpl uidGenerator;

    @Setup
    public void init() {
        Config config = new Config();
        config.setSegmentNum(64);
        config.setThreshold(30);
        config.setSegmentUidFromRemote(false);
        config.setSnowflakeUidFromRemote(false);
        uidGenerator = new InMemorySegmentUidGenerator(config, 10_000);
    }

    @Benchmark
    @Threads(1)
    public long segmentCacheThread1() {
        return uidGenerator.getUIDFromLocalCache(BIZ_CODE);
    }

    @Benchmark
    @Threads(8)
    public long segmentCacheThread8() {
        return uidGenerator.getUIDFromLocalCache(BIZ_CODE);
    }

    @Benchmark
    @Threads(32)
    public long segmentCacheThread32() {
        return uidGenerator.getUIDFromLocalCache(BIZ_CODE);
    }

    @Benchmark
    @Threads(100)
    public long segmentCacheThread100() {
        return uidGenerator.getUIDFromLocalCache(BIZ_CODE);
    }

    @TearDown
    public void shutdown() {
        uidGenerator.shutdown();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SegmentClientCachedBenchmark.class.getSimpleName())
            .result("result.json")
            .resultFormat(ResultFormatType.JSON)
            .build();
        new Runner(opt).run();
    }

    private static class InMemorySegmentUidGenerator extends SegmentUidGeneratorClientImpl {

        private final AtomicLong nextSegmentStart = new AtomicLong(1);
        private final int segmentLength;

        InMemorySegmentUidGenerator(Config config, int segmentLength) {
            super(config);
            this.segmentLength = segmentLength;
        }

        @Override
        public List<Segment> getSegments(String bizCode, int segmentNum) {
            List<Segment> segments = new ArrayList<>(segmentNum);
            for (int i = 0; i < segmentNum; i++) {
                segments.add(new Segment(nextSegmentStart.getAndAdd(segmentLength), segmentLength));
            }
            return segments;
        }
    }
}
