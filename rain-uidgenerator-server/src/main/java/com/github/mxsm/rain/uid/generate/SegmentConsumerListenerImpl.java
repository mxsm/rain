package com.github.mxsm.rain.uid.generate;


import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentConsumerListener;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
import com.github.mxsm.rain.uid.service.AllocationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mxsm
 * @date 2022/4/23 21:45
 * @Since 1.0.0
 */
@Service("segmentConsumerListenerImpl")
public class SegmentConsumerListenerImpl implements SegmentConsumerListener {

    @Autowired
    private AllocationService allocationService;

    @Override
    public void listener(SegmentPanel segmentPanel, int segmentNum) {
        String bizCode = segmentPanel.getBizCode();
        List<Segment> segments = allocationService.getSegments(bizCode, segmentNum);
        segmentPanel.addSegment(segments);
        segmentPanel.resetCounter();
    }
}
