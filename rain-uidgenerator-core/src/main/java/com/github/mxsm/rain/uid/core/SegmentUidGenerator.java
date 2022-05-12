package com.github.mxsm.rain.uid.core;

import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.segment.Segment;
import java.util.List;

/**
 * @author mxsm
 * @date 2022/5/1 15:52
 * @Since 1.0.0
 */
public interface SegmentUidGenerator {


    /**
     * get a unique ID of segment type
     *
     * @return UID
     * @throws UidGenerateException
     */
    long getUID(String bizCode) throws UidGenerateException;


    /**
     * get segment list
     * @param bizCode
     * @param segmentNum
     * @return
     */
    List<Segment> getSegments(String bizCode, int segmentNum);
}
