package com.github.mxsm.rain.uid.client;

import com.github.mxsm.rain.uid.client.service.SegmentUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.client.service.SnowflakeUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.core.snowflake.BitsAllocator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mxsm
 * @date 2022/4/30 20:46
 * @Since 1.0.0
 */
public class UidClientImpl implements UidClient {

    private SegmentUidGeneratorClientImpl segmentService;

    private SnowflakeUidGeneratorClientImpl snowflakeService;

    private boolean segmentUidFromRemote;

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
        new ThreadFactory() {
            AtomicInteger threadNum = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable run) {
                Thread thread = new Thread(run, "async-get-segments-thread-" + threadNum.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        });

    public UidClientImpl(Config config) {

        this.segmentService = new SegmentUidGeneratorClientImpl(config.getUidGeneratorServerUir(),
            config.getSegmentNum(), config.getThreshold(), executorService);
        this.snowflakeService = new SnowflakeUidGeneratorClientImpl(config.getUidGeneratorServerUir(),
            config.getEpoch(), config.isTimeBitsSecond(), config.isSnowflakeUidFromRemote(),
            new BitsAllocator(config.getTimestampBits(), config.getMachineIdBits(), config.getSequenceBits()));
        this.segmentUidFromRemote = config.isSegmentUidFromRemote();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executorService.shutdown()));
    }

    @Override
    public long getSegmentUid(String bizCode, boolean fromRemote) {
        return fromRemote ? segmentService.getUID(bizCode) : segmentService.getUIDFromLocalCache(bizCode);
    }

    @Override
    public long getSegmentUid(String bizCode) {
        return getSegmentUid(bizCode, this.segmentUidFromRemote);
    }

    @Override
    public long getSnowflakeUid() {
        return snowflakeService.getUID();
    }

    @Override
    public SnowflakeUidParsedResult parseSnowflakeUid(long uid) {
        return snowflakeService.parseUID(uid);
    }
}
