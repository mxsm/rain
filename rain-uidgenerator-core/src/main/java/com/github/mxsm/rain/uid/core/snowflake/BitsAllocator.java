package com.github.mxsm.rain.uid.core.snowflake;

import com.github.mxsm.rain.uid.core.exception.UidGenerateException;

/**
 * @author mxsm
 * @date 2022/5/3 16:11
 * @Since 1.0.0
 */
public class BitsAllocator {

    /**
     * total 64 bits
     */
    public static final int TOTAL_BITS = 1 << 6;


    private int signBits = 1;

    private final int timestampBits;

    private final int machineIdBits;

    private final int sequenceBits;

    /**
     * max value for timestamp machineId & sequence
     */
    private final long maxTimestamp;

    private final long maxMachineId;

    private final long maxSequence;

    /**
     * Shift for timestamp & workerId
     */
    private final int timestampShift;

    private final int machineIdShift;

    private long machineId;

    public BitsAllocator(int timestampBits, int machineIdBits, int sequenceBits) {
        this(timestampBits, machineIdBits, sequenceBits, -1);
    }

    public BitsAllocator(int timestampBits, int machineIdBits, int sequenceBits, long machineId) {
        // make sure allocated 64 bits
        int allocateTotalBits = signBits + timestampBits + machineIdBits + sequenceBits;
        if (allocateTotalBits > TOTAL_BITS) {
            throw new UidGenerateException("allocate less than or equal to 63 bits");
        }
        // initialize bits
        this.timestampBits = timestampBits;
        this.machineIdBits = machineIdBits;
        this.sequenceBits = sequenceBits;

        // initialize max value
        this.maxTimestamp = ~(-1L << timestampBits);
        this.maxMachineId = ~(-1L << machineIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // initialize shift
        this.timestampShift = machineIdBits + sequenceBits;
        this.machineIdShift = sequenceBits;

        this.machineId = machineId;
    }

    public long allocate(long timestamp, long machineId, long sequence) {
        return (timestamp << timestampShift) | (machineId << machineIdShift) | sequence;
    }

    public long allocate(long timestamp, long sequence) {
        return (timestamp << timestampShift) | (machineId << machineIdShift) | sequence;
    }

    public int getSignBits() {
        return signBits;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public int getMachineIdBits() {
        return machineIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    public long getMaxMachineId() {
        return maxMachineId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        if (machineId < 0 || machineId > maxMachineId) {
            throw new UidGenerateException("machine id not within the scope of 0 - " + maxMachineId);
        }
        this.machineId = machineId;
    }

}
