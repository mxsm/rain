package com.github.mxsm.rain.uid.core.common;

/**
 * @author mxsm
 * @date 2022/5/6 10:46
 * @Since 1.0.0
 */
public class SnowflakeUidParsedResult {

    private long uid;

    private String timestamp; //format yyyy-MM-dd

    private long machineId;

    private long sequence;

    public SnowflakeUidParsedResult(long uid, String timestamp, long machineId, long sequence) {
        this.uid = uid;
        this.timestamp = timestamp;
        this.machineId = machineId;
        this.sequence = sequence;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
