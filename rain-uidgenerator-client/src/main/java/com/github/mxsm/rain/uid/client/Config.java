package com.github.mxsm.rain.uid.client;

/**
 * @author mxsm
 * @date 2022/5/3 17:00
 * @Since 1.0.0
 */
public class Config {

    //server url
    private String uidGeneratorServerUir;

    //get segment number from remote server
    private int segmentNum = 16;

    //threshold of get segment from remote
    private int threshold = 30;

    //bit‘s length of snowflake timestamp
    private int timestampBits = 41;

    //bit‘s length of  snowflake machine id
    private int machineIdBits = 10;

    //bit‘s length of  snowflake sequence
    private int sequenceBits = 12;

    //setting timestamp is second or millisecond
    private boolean timeBitsSecond = false;

    // start epoch, and must before now
    private String epoch = "2015-05-01";

    //Whether to obtain the snowflake ID remotely or locally
    private boolean snowflakeUidFromRemote = true;

    //Whether to obtain the segment ID remotely or locally
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
