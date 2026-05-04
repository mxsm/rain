package com.github.mxsm.rain.uid.core.segment;


import com.github.mxsm.rain.uid.core.exception.SegmentOutOfBoundaryException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mxsm
 * @date 2022/4/21 22:50
 * @Since 1.0.0
 */
public class Segment {

    public static final long EXHAUSTED = Long.MIN_VALUE;

    //this segment start number
    private long segmentStartNum;

    //this segment length
    private int length;

    private AtomicInteger increment = new AtomicInteger(0);

    //segment status
    private volatile boolean isOk = true;

    public Segment() {
    }

    public Segment(long segmentStartNum, int length) {
        this.segmentStartNum = segmentStartNum;
        this.length = length;
    }

    public long createSegmentUid(){
        long uid = tryCreateSegmentUid();
        if (uid == EXHAUSTED) {
            throw new SegmentOutOfBoundaryException();
        }
        return uid;
    }

    public long tryCreateSegmentUid() {
        while (true) {
            int incrementNum = increment.get();
            if (incrementNum >= length) {
                isOk = false;
                return EXHAUSTED;
            }
            if (increment.compareAndSet(incrementNum, incrementNum + 1)) {
                return segmentStartNum + incrementNum;
            }
        }
    }

    public boolean isOk() {
        return isOk;
    }

    public long getSegmentStartNum() {
        return segmentStartNum;
    }

    public void setSegmentStartNum(long segmentStartNum) {
        this.segmentStartNum = segmentStartNum;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
