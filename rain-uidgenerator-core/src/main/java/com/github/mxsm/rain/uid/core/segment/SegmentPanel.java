package com.github.mxsm.rain.uid.core.segment;

import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.exception.UidUnavailableException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mxsm
 * @date 2022/4/21 22:59
 * @Since 1.0.0
 */
public class SegmentPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentPanel.class);

    private BlockingQueue<Segment> segmentQueue;

    private volatile Segment currentSegment;

    private Lock lock = new ReentrantLock();

    private SegmentConsumerListener listener;

    private String bizCode;

    private int threshold;

    private volatile int counter = 1;

    private int capacity;

    private long waitTimeoutMillis;

    private final AtomicBoolean refillInProgress = new AtomicBoolean(false);

    public SegmentPanel(String bizCode, int capacity, int threshold, List<Segment> segments,
        SegmentConsumerListener listener) {
        this(bizCode, capacity, threshold, segments, listener, 3000L);
    }

    public SegmentPanel(String bizCode, int capacity, int threshold, List<Segment> segments,
        SegmentConsumerListener listener, long waitTimeoutMillis) {

        this.bizCode = bizCode;
        this.capacity = capacity <= 0 ? 16 : capacity;
        this.segmentQueue = new ArrayBlockingQueue<>(this.capacity);
        if (segments != null) {
            this.segmentQueue.addAll(segments);
        }
        this.listener = listener;
        this.threshold = Math.max(0, Math.min(100, threshold));
        this.waitTimeoutMillis = waitTimeoutMillis <= 0 ? 3000L : waitTimeoutMillis;
        this.currentSegment = this.segmentQueue.poll();
    }

    public long getUid() {
        while (true) {
            Segment segment = this.currentSegment;
            if (segment != null) {
                long uid = segment.tryCreateSegmentUid();
                if (uid != Segment.EXHAUSTED) {
                    maybeRequestRefill();
                    return uid;
                }
            }
            switchSegment();
        }
    }

    private void switchSegment() {
        lock.lock();
        try {
            if (this.currentSegment != null && this.currentSegment.isOk()) {
                return;
            }
            maybeRequestRefill();
            this.currentSegment = segmentQueue.poll(waitTimeoutMillis, TimeUnit.MILLISECONDS);
            if (this.currentSegment == null) {
                throw new UidUnavailableException(ErrorCode.UID_UNAVAILABLE,
                    "No segment is available for bizCode " + bizCode);
            }
            ++counter;
            maybeRequestRefill();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UidUnavailableException(ErrorCode.UID_UNAVAILABLE,
                "Interrupted while waiting for segment for bizCode " + bizCode, ex);
        } finally {
            lock.unlock();
        }
    }

    private void maybeRequestRefill() {
        if (listener == null) {
            return;
        }
        int remaining = segmentQueue.size();
        if (currentSegment != null && currentSegment.isOk()) {
            remaining++;
        }
        if ((remaining * 100) / capacity > threshold) {
            return;
        }
        if (refillInProgress.compareAndSet(false, true)) {
            int segmentNum = Math.max(1, capacity - remaining);
            listener.listener(this, segmentNum);
        }
    }

    public boolean addSegment(Segment segment) {
        boolean added = this.segmentQueue.offer(segment);
        if (!added) {
            LOGGER.warn("Segment queue is full, discard allocated segment. bizCode={}", bizCode);
        }
        return added;
    }

    public int addSegment(List<Segment> segments) {
        if (segments == null) {
            return 0;
        }
        int added = 0;
        for (Segment segment : segments) {
            if (this.addSegment(segment)) {
                added++;
            }
        }
        return added;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void resetCounter() {
        this.counter = 0;
    }

    public void refillFinished() {
        this.refillInProgress.set(false);
    }

    public int availableSegments() {
        int available = segmentQueue.size();
        if (currentSegment != null && currentSegment.isOk()) {
            available++;
        }
        return available;
    }

}
