package com.github.mxsm.rain.uid.client.service;

import com.github.mxsm.rain.uid.client.Config;
import com.github.mxsm.rain.uid.client.Http2Requester;
import com.github.mxsm.rain.uid.client.utils.UrlUtils;
import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.snowflake.AbstractSnowflakeUidGenerator;


/**
 * @author mxsm
 * @date 2022/5/3 16:45
 * @Since 1.0.0
 */
public class SnowflakeUidGeneratorClientImpl extends AbstractSnowflakeUidGenerator implements SnowflakeUidGenerator {

    public static final String SNOWFLAKE_UDI_PATH = "/api/v1/snowflake/uid";

    private String uidGeneratorServerUir;

    private boolean snowflakeUidFromRemote;

    private String host;

    private int port;

    public SnowflakeUidGeneratorClientImpl(Config config) {
        super(config.getEpoch(), config.isTimeBitsSecond(),config.getTimestampBits(), config.getMachineIdBits(), config.getSequenceBits());
        this.uidGeneratorServerUir = config.getUidGeneratorServerUir();
        this.snowflakeUidFromRemote = config.isSnowflakeUidFromRemote();
        super.getBitsAllocator().setMachineId(getMachineId());
        parseURL();
    }

    private void parseURL() {

        if(!snowflakeUidFromRemote){
            return;
        }
        String[] sts = UrlUtils.parseUriAndPort(this.uidGeneratorServerUir);
        this.host = sts[0];
        this.port = Integer.parseInt(sts[1]);
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
            String content = Http2Requester.executeGET(host, port, SNOWFLAKE_UDI_PATH);
            return Long.parseLong(content);
        } catch (Exception e) {
            throw new UidGenerateException("Get Uid from remote [URL=" + SNOWFLAKE_UDI_PATH + "] error", e);
        }
    }
}
