package com.github.mxsm.rain.uid.generate;

import com.github.mxsm.rain.uid.config.SegmentUidGeneratorConfig;
import com.github.mxsm.rain.uid.core.segment.AbstractSegmentUidGenerator;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentConsumerListener;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
import com.github.mxsm.rain.uid.service.AllocationService;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author mxsm
 * @date 2022/4/17 16:28
 * @Since 1.0.0
 */
@Service("uidGenerator")
public class SegmentUidGeneratorServerImpl extends AbstractSegmentUidGenerator {

    private final AllocationService allocationService;

    private final SegmentUidGeneratorConfig config;

    private final SegmentConsumerListener listener;

    public SegmentUidGeneratorServerImpl(SegmentUidGeneratorConfig config, AllocationService allocationService,
        SegmentConsumerListener listener) {
        super(config.getCacheSize());
        this.config = config;
        this.allocationService = allocationService;
        this.listener = listener;
    }

    @Override
    public SegmentPanel createSegmentPanel(String bizCode, int segmentNum) {
        return new SegmentPanel(bizCode, segmentNum, config.getThreshold(), getSegments(bizCode, segmentNum), listener,
            config.getWaitTimeoutMillis());
    }

    @Override
    public List<Segment> getSegments(String bizCode, int segmentNum) {
        return allocationService.getSegments(bizCode, segmentNum);
    }
}
