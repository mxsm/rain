package com.github.mxsm.rain.uid.client;

import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;


/**
 * @author mxsm
 * @date 2022/4/30 15:54
 * @Since 1.0.0
 */
public interface UidClient {

    /**
     * obtain segment id
     *
     * @param bizCode    segment id biz code
     * @param fromRemote if true obtain segment id from remote or local
     * @return segment id
     */
    long getSegmentUid(String bizCode, boolean fromRemote);

    /**
     * obtain segment id from remote or local depend on {@link Config} field segmentUidFromRemote is ture or false
     * segmentUidFromRemote default value is true
     * @param bizCode segment id biz code
     * @return segment id
     */
    long getSegmentUid(String bizCode);


    /**
     * obtain snowflake id from remote or local depend on {@link Config} field snowflakeUidFromRemote is ture or false.
     * snowflakeUidFromRemote default value is true
     * @return snowflake id
     */
    long getSnowflakeUid();

    /**
     * parse uid to  {@link SnowflakeUidParsedResult}
     * @param uid
     * @return
     */
    SnowflakeUidParsedResult parseSnowflakeUid(long uid);


    static UidClientBuilder builder() {
        return new UidClientBuilder();
    }

}
