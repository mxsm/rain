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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
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
@Measurement(iterations = 3, time = 10)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SnowflakeClientLocalBenchmark {

    private UidClient uidClient;

    @Setup
    public void init() {
        uidClient = UidClient.builder().isSnowflakeUidFromRemote(false).build();
    }

    @Benchmark
    @Threads(1)
    public void snowflakeClientLocalBenchmarksThread1() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(4)
    public void snowflakeClientLocalBenchmarksThread4() {
        uidClient.getSnowflakeUid();
    }


    @Benchmark
    @Threads(8)
    public void snowflakeClientLocalBenchmarksThread8() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(16)
    public void snowflakeClientLocalBenchmarksThread16() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(32)
    public void snowflakeClientLocalBenchmarksThread32() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(50)
    public void snowflakeClientLocalBenchmarksThread50() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(100)
    public void snowflakeClientLocalBenchmarksThread100() {
        uidClient.getSnowflakeUid();
    }

    @Benchmark
    @Threads(200)
    public void snowflakeClientLocalBenchmarksThread200() {
        uidClient.getSnowflakeUid();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SnowflakeClientLocalBenchmark.class.getSimpleName())
            .result("result.json")
            .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }

}
