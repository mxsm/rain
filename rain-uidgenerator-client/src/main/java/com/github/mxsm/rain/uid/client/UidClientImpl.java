package com.github.mxsm.rain.uid.client;

import com.github.mxsm.rain.uid.client.service.SegmentUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.client.service.SnowflakeUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;

/**
 * @author mxsm
 * @date 2022/4/30 20:46
 * @Since 1.0.0
 */
public class UidClientImpl implements UidClient {

    private SegmentUidGeneratorClientImpl segmentService;

    private SnowflakeUidGeneratorClientImpl snowflakeService;

    private boolean segmentUidFromRemote;

    public UidClientImpl(Config config) {

        this.segmentService = new SegmentUidGeneratorClientImpl(config);
        this.snowflakeService = new SnowflakeUidGeneratorClientImpl(config);
        this.segmentUidFromRemote = config.isSegmentUidFromRemote();
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
        segmentService.shutdown();
    }
}
