package com.github.mxsm.rain.uid.core.segment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author mxsm
 * @date 2022/4/17 16:30
 * @Since 1.0.0
 */
public class SegmentUidGeneratorCacheDefaultImpl implements SegmentUidGeneratorCache {

    private final Map<String, SegmentPanel> caches = new ConcurrentHashMap<>();

    @Override
    public long getUidFromCacheOrElse(String bizCode, Supplier<SegmentPanel> supplier){

        SegmentPanel segmentPanel = caches.computeIfAbsent(bizCode, key -> supplier.get());
        return segmentPanel.getUid();
    }

}
