package com.github.mxsm.rain.jmh;

import com.github.mxsm.rain.uid.client.UidClient;
import java.util.concurrent.TimeUnit;
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

/**
 * snowflake id client local benchmark
 *
 * @author mxsm
 * @date 2022/5/14 22:06
 * @Since 1.0.0
 */

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 4)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SegmentClientCachedBenchmark {

    private UidClient uidClient;

    @Setup
    public void init() {
        uidClient = UidClient.builder().setUidGeneratorServerUir("172.23.186.56:8080").setSegmentNum(32).isSegmentUidFromRemote(false).build();
    }

    @Benchmark
    @Threads(1)
    public void snowflakeClientLocalBenchmarksThread1() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(4)
    public void snowflakeClientLocalBenchmarksThread4() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }


    @Benchmark
    @Threads(8)
    public void snowflakeClientLocalBenchmarksThread8() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(16)
    public void snowflakeClientLocalBenchmarksThread16() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(32)
    public void snowflakeClientLocalBenchmarksThread32() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(50)
    public void snowflakeClientLocalBenchmarksThread50() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(100)
    public void snowflakeClientLocalBenchmarksThread100() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @Benchmark
    @Threads(200)
    public void snowflakeClientLocalBenchmarksThread200() {
        uidClient.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
    }

    @TearDown
    public void shutdown(){
        uidClient.shutdown();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SegmentClientCachedBenchmark.class.getSimpleName())
            .result("result.json")
            .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }

}
