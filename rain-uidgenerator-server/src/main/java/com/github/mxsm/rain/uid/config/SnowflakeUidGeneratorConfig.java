package com.github.mxsm.rain.uid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author mxsm
 * @date 2022/4/30 7:48
 * @Since 1.0.0
 */
@ConfigurationProperties(prefix = "mxsm.uid.snowflake")
@Configuration
public class SnowflakeUidGeneratorConfig {

    private int timestampBits = 41;
    private int machineIdBits = 10;
    private int sequenceBits = 12;

    private boolean container;

    private boolean timeBitsSecond;

    private String epoch;

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public void setTimestampBits(int timestampBits) {
        this.timestampBits = timestampBits;
    }

    public int getMachineIdBits() {
        return machineIdBits;
    }

    public void setMachineIdBits(int machineIdBits) {
        this.machineIdBits = machineIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public void setSequenceBits(int sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
    }

    public boolean isTimeBitsSecond() {
        return timeBitsSecond;
    }

    public void setTimeBitsSecond(boolean timeBitsSecond) {
        this.timeBitsSecond = timeBitsSecond;
    }
}
