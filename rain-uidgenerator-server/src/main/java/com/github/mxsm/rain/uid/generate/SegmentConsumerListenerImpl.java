package com.github.mxsm.rain.uid.generate;


import com.github.mxsm.rain.uid.config.SegmentUidGeneratorConfig;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentConsumerListener;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import com.github.mxsm.rain.uid.service.AllocationService;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * @author mxsm
 * @date 2022/4/23 21:45
 * @Since 1.0.0
 */
@Service("segmentConsumerListenerImpl")
public class SegmentConsumerListenerImpl implements SegmentConsumerListener {

    private final AllocationService allocationService;

    private final UidMetrics uidMetrics;

    private final ExecutorService executorService;

    public SegmentConsumerListenerImpl(AllocationService allocationService, SegmentUidGeneratorConfig config,
        UidMetrics uidMetrics) {
        this.allocationService = allocationService;
        this.uidMetrics = uidMetrics;
        int threads = Math.max(1, config.getPrefetchThreads());
        this.executorService = Executors.newFixedThreadPool(threads, new PrefetchThreadFactory());
    }

    @Override
    public void listener(SegmentPanel segmentPanel, int segmentNum) {
        executorService.submit(() -> {
            try {
                String bizCode = segmentPanel.getBizCode();
                List<Segment> segments = allocationService.getSegments(bizCode, segmentNum);
                int added = segmentPanel.addSegment(segments);
                uidMetrics.recordSegmentDiscarded(segments.size() - added);
                segmentPanel.resetCounter();
            } finally {
                segmentPanel.refillFinished();
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private static class PrefetchThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "segment-prefetch-" + threadNum.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
