package com.github.mxsm.rain.uid.core;

import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;

/**
 * @author mxsm
 * @date 2022/5/1 15:52
 * @Since 1.0.0
 */
public interface SnowflakeUidGenerator {

    /**
     * Get a unique ID for snowflake
     *
     * @return UID
     * @throws UidGenerateException
     */
    long getUID() throws UidGenerateException;


    /**
     * parse uid to string
     * @param uid
     * @return
     */
    SnowflakeUidParsedResult parseUID(long uid);
}
