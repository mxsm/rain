package com.github.mxsm.rain.uid.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxsm.rain.uid.client.Config;
import com.github.mxsm.rain.uid.client.Http2Requester;
import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.snowflake.AbstractSnowflakeUidGenerator;


/**
 * @author mxsm
 * @date 2022/5/3 16:45
 * @Since 1.0.0
 */
public class SnowflakeUidGeneratorClientImpl extends AbstractSnowflakeUidGenerator implements SnowflakeUidGenerator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String SNOWFLAKE_UDI_PATH = "/api/v1/snowflake/uid";

    private Config config;

    private boolean snowflakeUidFromRemote;

    public SnowflakeUidGeneratorClientImpl(Config config) {
        super(config.getEpoch(), config.isTimeBitsSecond(),config.getTimestampBits(), config.getMachineIdBits(), config.getSequenceBits());
        this.config = config;
        this.snowflakeUidFromRemote = config.isSnowflakeUidFromRemote();
        super.getBitsAllocator().setMachineId(getMachineId());
    }

    @Override
    public long getMachineId() {
        return randomMachineId();
    }

    /**
     * Get a unique ID for snowflake
     *
     * @return UID
     * @throws UidGenerateException
     */
    @Override
    public long getUID() throws UidGenerateException {
        return snowflakeUidFromRemote ? getUidFromRemote() : super.getUID();
    }

    public long getUidFromRemote() {
        try {
            String content = Http2Requester.executeGET(config, SNOWFLAKE_UDI_PATH);
            Result<Long> result = OBJECT_MAPPER.readValue(content, new TypeReference<>() {
            });
            if (!result.isSuccess()) {
                throw new UidGenerateException(ErrorCode.UPSTREAM_UNAVAILABLE,
                    "Snowflake server returned " + result.getCode() + ": " + result.getMsg());
            }
            return result.getData();
        } catch (Exception e) {
            if (e instanceof UidGenerateException uidGenerateException) {
                throw uidGenerateException;
            }
            throw new UidGenerateException("Get Uid from remote [URL=" + SNOWFLAKE_UDI_PATH + "] error", e);
        }
    }
}
