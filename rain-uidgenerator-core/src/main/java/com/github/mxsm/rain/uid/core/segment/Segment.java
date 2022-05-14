package com.github.mxsm.rain.uid.core.segment;


import com.github.mxsm.rain.uid.core.exception.SegmentOutOfBoundaryException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mxsm
 * @date 2022/4/21 22:50
 * @Since 1.0.0
 */
public class Segment {

    //this segment start number
    private long segmentStartNum;

    //this segment length
    private int length;

    private AtomicInteger increment = new AtomicInteger(0);

    //segment status
    private volatile boolean isOk = true;

    public Segment(long segmentStartNum, int length) {
        this.segmentStartNum = segmentStartNum;
        this.length = length;
    }

    public long createSegmentUid(){
        if(!isOk){
            throw new SegmentOutOfBoundaryException();
        }
        int incrementNum = increment.getAndIncrement();
        if(incrementNum >= length){
            isOk = false;
            throw new SegmentOutOfBoundaryException();
        }
        return segmentStartNum + incrementNum;
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
