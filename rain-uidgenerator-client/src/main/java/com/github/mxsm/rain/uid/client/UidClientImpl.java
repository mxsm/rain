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
       // Runtime.getRuntime().addShutdownHook(new Thread(() -> executorService.shutdown()));
    }


    /**
     * obtain segment id
     *
     * @param bizCode    segment id biz code
     * @param fromRemote if true obtain segment id from remote or local
     * @return segment id
     */
    @Override
    public long getSegmentUid(String bizCode, boolean fromRemote) {
        return fromRemote ? segmentService.getUID(bizCode) : segmentService.getUIDFromLocalCache(bizCode);
    }

    /**
     * obtain segment id from remote or local depend on {@link Config} field segmentUidFromRemote is ture or false
     * segmentUidFromRemote default value is true
     *
     * @param bizCode segment id biz code
     * @return segment id
     */
    @Override
    public long getSegmentUid(String bizCode) {
        return getSegmentUid(bizCode, this.segmentUidFromRemote);
    }

    /**
     * obtain snowflake id from remote or local depend on {@link Config} field snowflakeUidFromRemote is ture or false.
     * snowflakeUidFromRemote default value is true
     *
     * @return snowflake id
     */
    @Override
    public long getSnowflakeUid() {
        return snowflakeService.getUID();
    }

    /**
     * parse uid to  {@link SnowflakeUidParsedResult}
     *
     * @param uid
     * @return
     */
    @Override
    public SnowflakeUidParsedResult parseSnowflakeUid(long uid) {
        return snowflakeService.parseUID(uid);
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
    }
}
