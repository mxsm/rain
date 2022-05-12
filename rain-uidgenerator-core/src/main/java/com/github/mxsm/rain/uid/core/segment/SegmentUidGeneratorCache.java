package com.github.mxsm.rain.uid.core.segment;

import java.util.function.Supplier;

/**
 * @author mxsm
 * @date 2022/4/30 21:57
 * @Since 1.0.0
 */
public interface SegmentUidGeneratorCache {

    long getUidFromCacheOrElse(String bizCode, Supplier<SegmentPanel> supplier);
}
