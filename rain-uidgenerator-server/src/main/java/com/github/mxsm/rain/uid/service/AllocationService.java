package com.github.mxsm.rain.uid.service;


import com.github.mxsm.rain.uid.core.segment.Segment;
import java.util.List;

/**
 * @author mxsm
 * @date 2022/4/30 7:18
 * @Since 1.0.0
 */
public interface AllocationService {

    List<Segment> getSegments(String bizCode, int segmentNum);

    boolean registerBizCode(String bizCode, int step);
}
