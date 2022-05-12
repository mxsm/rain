package com.github.mxsm.rain.uid.core.segment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author mxsm
 * @date 2022/4/17 16:30
 * @Since 1.0.0
 */
public class SegmentUidGeneratorCacheDefaultImpl implements SegmentUidGeneratorCache {

    private Map<String, SegmentPanel> caches = new HashMap<>();

    private Lock lock = new ReentrantLock();

    @Override
    public long getUidFromCacheOrElse(String bizCode, Supplier<SegmentPanel> supplier){

        SegmentPanel segmentPanel = caches.get(bizCode);
        if(segmentPanel == null){
            try {
                lock.lock();
                segmentPanel = caches.get(bizCode);
                if(segmentPanel == null){
                    segmentPanel = supplier.get();
                    caches.put(bizCode, segmentPanel);
                }
            } finally {
                lock.unlock();
            }
        }
        return segmentPanel.getUid();
    }

}
