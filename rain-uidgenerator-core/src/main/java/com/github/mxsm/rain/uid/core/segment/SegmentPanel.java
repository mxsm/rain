package com.github.mxsm.rain.uid.core.segment;

import com.github.mxsm.rain.uid.core.exception.SegmentOutOfBoundaryException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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

    private Logger LOGGER = LoggerFactory.getLogger(SegmentPanel.class);

    private BlockingQueue<Segment> segmentQueue;

    private volatile Segment currentSegment;

    private Lock lock = new ReentrantLock();

    private SegmentConsumerListener listener;

    private String bizCode;

    private int threshold;

    private volatile int counter = 1;

    private int capacity;

    public SegmentPanel(String bizCode, int capacity, int threshold, List<Segment> segments,
        SegmentConsumerListener listener) {

        this.bizCode = bizCode;
        this.capacity = capacity <= 0 ? 16 : capacity;
        this.segmentQueue = new ArrayBlockingQueue<>(this.capacity);
        this.segmentQueue.addAll(segments);
        this.listener = listener;
        this.threshold = threshold;
        this.currentSegment = this.segmentQueue.poll();
    }

    public long getUid() {
        while (true) {
            try {
                return this.currentSegment.createSegmentUid();
            } catch (Exception e) {
                if (e instanceof SegmentOutOfBoundaryException) {
                    try {
                        lock.lock();
                        if (this.currentSegment != null && !this.currentSegment.isOk()) {
                            if (((capacity - counter) * 100) / capacity <= threshold) {
                                listener.listener(this, counter);
                            }
                            this.currentSegment = segmentQueue.poll(3, TimeUnit.SECONDS);
                            if (this.currentSegment == null) {
                                return -1;
                            }
                            ++counter;
                        }
                    } catch (InterruptedException interruptedException) {
                        LOGGER.error("poll segment from segmentQueue error", interruptedException);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    try {
                        this.currentSegment = segmentQueue.poll(3, TimeUnit.SECONDS);
                        if (this.currentSegment == null) {
                            return -1;
                        }
                    } catch (InterruptedException ex) {
                        LOGGER.error("poll segment from segmentQueue error", ex);
                    }
                }

            }
        }
    }

    public void addSegment(Segment segment) {
        this.segmentQueue.offer(segment);
    }

    public void addSegment(List<Segment> segments) {
        for (Segment segment : segments) {
            this.addSegment(segment);
        }
    }

    public String getBizCode() {
        return bizCode;
    }

    public void resetCounter() {
        this.counter = 0;
    }

}
