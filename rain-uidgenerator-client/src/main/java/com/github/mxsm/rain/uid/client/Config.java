package com.github.mxsm.rain.uid.client;

/**
 * @author mxsm
 * @date 2022/5/3 17:00
 * @Since 1.0.0
 */
public class Config {

    private String uidGeneratorServerUir;

    private int segmentNum = 16;

    private int threshold = 30;

    private int timestampBits = 41;

    private int machineIdBits = 10;

    private int sequenceBits = 12;

    private boolean container = false;

    private boolean timeBitsSecond = false;

    private String epoch = "2015-05-01";

    private boolean snowflakeUidFromRemote = true;

    private boolean segmentUidFromRemote = true;

    public int getSegmentNum() {
        return segmentNum;
    }

    public void setSegmentNum(int segmentNum) {
        this.segmentNum = segmentNum;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
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

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public String getUidGeneratorServerUir() {
        return uidGeneratorServerUir;
    }

    public void setUidGeneratorServerUir(String uidGeneratorServerUir) {
        this.uidGeneratorServerUir = uidGeneratorServerUir;
    }

    public boolean isSnowflakeUidFromRemote() {
        return snowflakeUidFromRemote;
    }

    public void setSnowflakeUidFromRemote(boolean snowflakeUidFromRemote) {
        this.snowflakeUidFromRemote = snowflakeUidFromRemote;
    }

    public boolean isSegmentUidFromRemote() {
        return segmentUidFromRemote;
    }

    public void setSegmentUidFromRemote(boolean segmentUidFromRemote) {
        this.segmentUidFromRemote = segmentUidFromRemote;
    }
}
