package com.github.mxsm.rain.jmh;

import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
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
public class SegmentRefillBenchmark {

    private final AtomicLong nextSegmentStart = new AtomicLong(1);

    private SegmentPanel panel;

    @Setup
    public void init() {
        panel = new SegmentPanel("benchmark", 2, 100, nextSegments(2), (segmentPanel, segmentNum) -> {
            segmentPanel.addSegment(nextSegments(segmentNum));
            segmentPanel.refillFinished();
        });
    }

    @Benchmark
    @Threads(1)
    public long segmentRefillThread1() {
        return panel.getUid();
    }

    @Benchmark
    @Threads(8)
    public long segmentRefillThread8() {
        return panel.getUid();
    }

    @Benchmark
    @Threads(32)
    public long segmentRefillThread32() {
        return panel.getUid();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SegmentRefillBenchmark.class.getSimpleName())
            .result("result.json")
            .resultFormat(ResultFormatType.JSON)
            .build();
        new Runner(opt).run();
    }

    private List<Segment> nextSegments(int segmentNum) {
        return java.util.stream.IntStream.range(0, segmentNum)
            .mapToObj(index -> new Segment(nextSegmentStart.getAndIncrement(), 1))
            .toList();
    }
}
