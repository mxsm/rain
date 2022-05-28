package com.github.mxsm.rain.uid.core.snowflake;

import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.core.utils.DateUtils;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mxsm
 * @date 2022/5/3 16:09
 * @Since 1.0.0
 */
public abstract class AbstractSnowflakeUidGenerator implements SnowflakeUidGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSnowflakeUidGenerator.class);

    private String epoch;

    private long epochTime;

    private volatile long lastTimestamp = -1L;

    private volatile long seqNum = 0;

    private AtomicLong lastTimestampAtomic = new AtomicLong(-1);

    private AtomicLong seqNumAtomic = new AtomicLong(0);

    private boolean timeBitsSecond;

    private BitsAllocator bitsAllocator;

    private Object lockStandard = new Object();

    private Object lockExt = new Object();

    private Lock lock = new ReentrantLock();

    public AbstractSnowflakeUidGenerator(String epoch, boolean timeBitsSecond, int timestampBits, int machineIdBits, int sequenceBits) {
        this.epoch = epoch;
        this.timeBitsSecond = timeBitsSecond;
        long epochMill = DateUtils.parseDate(epoch, "yyyy-MM-dd").getTime();
        this.epochTime = timeBitsSecond ? TimeUnit.MILLISECONDS.toSeconds(epochMill) : epochMill;
        this.bitsAllocator = new BitsAllocator(timestampBits, machineIdBits, sequenceBits);
    }

    public BitsAllocator getBitsAllocator() {
        return bitsAllocator;
    }

    /**
     * Get a unique ID for snowflake
     *
     * @return UID
     * @throws UidGenerateException
     */
    @Override
    public long getUID() throws UidGenerateException {
        return timeBitsSecond ? nextIdExt() : nextIdStandard();
    }

    /**
     * parse uid to string
     *
     * @param uid
     * @return
     */
    @Override
    public SnowflakeUidParsedResult parseUID(long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getMachineIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        // parse UID
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long machineId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
        long deltaTime = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(
            timeBitsSecond ? TimeUnit.SECONDS.toMillis(epochTime + deltaTime) : epochTime + deltaTime);
        String thatTimeStr = DateUtils.formatByDateTimePattern(thatTime);

        // format as string
        return new SnowflakeUidParsedResult(uid, thatTimeStr, machineId, sequence);
    }

    public abstract long getMachineId();

    private long nextIdStandard() {

        synchronized (lockStandard) {
            long currentTimestamp = System.currentTimeMillis();
            //time the callback
            if (currentTimestamp < lastTimestamp) {
                long offset = lastTimestamp - currentTimestamp;
                if (offset <= 1000L) {
                    currentTimestamp = tillNextMillis(lastTimestamp);
                } else {
                    // offset > 1000ms
                    LOGGER.error("Time rollback exceeds 1 second");
                    return -3;
                }
            }
            if (currentTimestamp == lastTimestamp) {
                seqNum = (seqNum + 1) & bitsAllocator.getMaxSequence();
                if (seqNum == 0) {
                    currentTimestamp = tillNextMillis(lastTimestamp);
                }
            } else {
                //reset seqNum
                seqNum = 0;
            }
            lastTimestamp = currentTimestamp;
            return bitsAllocator.allocate(currentTimestamp - epochTime, seqNum);
        }
    }

    private long nextIdStandardLock() {

        try {
            lock.lock();
            long currentTimestamp = System.currentTimeMillis();
            //Time the callback
            if (currentTimestamp < lastTimestamp) {
                long offset = lastTimestamp - currentTimestamp;
                if (offset <= 1000L) {
                    currentTimestamp = tillNextMillis(lastTimestamp);
                } else {
                    // offset > 1000ms
                    LOGGER.error("Time rollback exceeds 1 second");
                    return -3;
                }
            }
            if (currentTimestamp == lastTimestamp) {
                seqNum = (seqNum + 1) & bitsAllocator.getMaxSequence();
                if (seqNum == 0) {
                    currentTimestamp = tillNextMillis(lastTimestamp);
                }
            } else {
                //reset seqNum
                seqNum = 0;
            }
            lastTimestamp = currentTimestamp;
            return bitsAllocator.allocate(currentTimestamp - epochTime, seqNum);
        } finally {
            lock.unlock();
        }
    }

    private long nextIdStandardOptimisticLock() {

        for (; ; ) {
            long lastCurrent = lastTimestampAtomic.get();
            long headSeqNum = seqNumAtomic.get();
            long currentTimestamp = System.currentTimeMillis();

            //Time the callback
            if (currentTimestamp < lastCurrent) {
                long offset = lastCurrent - currentTimestamp;
                if (offset <= 1000L) {
                    currentTimestamp = tillNextMillis(lastCurrent);
                } else {
                    // offset > 1000ms
                    LOGGER.error("Time rollback exceeds 1 second");
                    return -3;
                }
            }
            long nextSeqNum = 0;
            if (currentTimestamp == lastCurrent) {
                nextSeqNum = (headSeqNum + 1) & bitsAllocator.getMaxSequence();
                if (nextSeqNum == 0) {
                    currentTimestamp = tillNextMillis(lastCurrent);
                }
            }
            boolean flag = lastTimestampAtomic.compareAndSet(lastCurrent, currentTimestamp) &
                seqNumAtomic.compareAndSet(headSeqNum, nextSeqNum);
            if (!flag) {
                continue;
            }
            return bitsAllocator.allocate(currentTimestamp - epochTime, nextSeqNum);
        }
    }

    private long tillNextMillis(long lastCurrent) {
        long currentTimestamp;
        currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= lastCurrent) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }

    private long nextIdExt() {

        synchronized (lockExt) {
            long currentSecond = getCurrentSecond();

            // clock moved backwards, refuse to generate uid
            if (currentSecond < lastTimestamp) {
                long refusedSeconds = lastTimestamp - currentSecond;
                LOGGER.error("Time rollback exceeds " + refusedSeconds + " second");
                return -2;
            }

            // at the same second, increase sequence
            if (currentSecond == lastTimestamp) {
                seqNum = (seqNum + 1) & bitsAllocator.getMaxSequence();
                // Exceed the max sequence, we wait the next second to generate uid
                if (seqNum == 0) {
                    currentSecond = getNextSecond(lastTimestamp);
                }
            } else {
                // At the different second, sequence restart from zero
                seqNum = 0L;
            }
            lastTimestamp = currentSecond;
            // Allocate bits for UID
            return bitsAllocator.allocate(currentSecond - epochTime, seqNum);
        }
    }

    private long nextIdExtLock() {

        try {
            lock.lock();
            long currentSecond = getCurrentSecond();

            // Clock moved backwards, refuse to generate uid
            if (currentSecond < lastTimestamp) {
                long refusedSeconds = lastTimestamp - currentSecond;
                LOGGER.error("Time rollback exceeds " + refusedSeconds + " second");
                return -2;
            }

            // At the same second, increase sequence
            if (currentSecond == lastTimestamp) {
                LOGGER.info("currentSecond={}", currentSecond);
                seqNum = (seqNum + 1) & bitsAllocator.getMaxSequence();
                // Exceed the max sequence, we wait the next second to generate uid
                if (seqNum == 0) {
                    currentSecond = getNextSecond(lastTimestamp);
                }
            } else {
                // At the different second, sequence restart from zero
                seqNum = 0L;
            }
            lastTimestamp = currentSecond;
            // Allocate bits for UID
            return bitsAllocator.allocate(currentSecond - epochTime, seqNum);
        } finally {
            lock.unlock();
        }
    }

    private long nextIdExtOptimisticLock() {

        for (; ; ) {
            long lastCurrent = lastTimestampAtomic.get();
            long currentSecond = getCurrentSecond();
            long headSeqNum = seqNumAtomic.get();
            // Clock moved backwards, refuse to generate uid
            if (currentSecond < lastCurrent) {
                long refusedSeconds = lastCurrent - currentSecond;
                LOGGER.error("Time rollback exceeds " + refusedSeconds + " second");
                return -2;
            }

            long nextSeqNum = 0L;
            // At the same second, increase sequence
            if (currentSecond == lastCurrent) {
                nextSeqNum = (headSeqNum + 1) & bitsAllocator.getMaxSequence();
                // Exceed the max sequence, we wait the next second to generate uid
                if (nextSeqNum == 0) {
                    currentSecond = getNextSecond(lastCurrent);
                }
            } else {
                // At the different second, sequence restart from zero
                nextSeqNum = 0L;
            }
            boolean flag = lastTimestampAtomic.compareAndSet(lastCurrent, currentSecond) &
                seqNumAtomic.compareAndSet(headSeqNum, nextSeqNum);
            if (!flag) {
                continue;
            }
            // Allocate bits for UID
            return bitsAllocator.allocate(currentSecond - epochTime, nextSeqNum);
        }
    }


    /**
     * Get next millisecond
     */
    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }
        return timestamp;
    }

    /**
     * Get current second
     */
    private long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochTime > bitsAllocator.getMaxTimestamp()) {
            throw new UidGenerateException(
                "Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond + ", epoch: " + epoch);
        }
        return currentSecond;
    }

    protected long randomMachineId() {
        long machineId = (long) (Math.random() * getBitsAllocator().getMaxMachineId());
        LOGGER.info("Random machine id is {}", machineId);
        return machineId;
    }

}
