package com.github.mxsm.rain.uid.core.segment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.mxsm.rain.uid.core.exception.UidUnavailableException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class SegmentPanelTest {

    @Test
    void refillsOnceWhenInventoryReachesThreshold() {
        AtomicInteger refillCount = new AtomicInteger();
        AtomicLong nextStart = new AtomicLong(100);
        SegmentPanel panel = new SegmentPanel("biz", 2, 50, List.of(new Segment(1, 2)), (segmentPanel, segmentNum) -> {
            refillCount.incrementAndGet();
            segmentPanel.addSegment(new Segment(nextStart.getAndAdd(100), 2));
            segmentPanel.refillFinished();
        });

        assertEquals(1, panel.getUid());
        assertEquals(2, panel.getUid());
        assertEquals(100, panel.getUid());
        assertTrue(refillCount.get() >= 1);
    }

    @Test
    void generatesUniqueIdsUnderConcurrency() throws Exception {
        AtomicLong nextStart = new AtomicLong(1);
        SegmentPanel panel = new SegmentPanel("biz", 8, 50, segments(nextStart, 8), (segmentPanel, segmentNum) -> {
            segmentPanel.addSegment(segments(nextStart, segmentNum));
            segmentPanel.refillFinished();
        });
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    ids.add(panel.getUid());
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(800, ids.size());
        assertEquals(800, new HashSet<>(ids).size());
    }

    @Test
    void throwsUnavailableWhenNoSegmentArrivesBeforeTimeout() {
        SegmentPanel panel = new SegmentPanel("biz", 1, 50, List.of(), null, 10);

        assertThrows(UidUnavailableException.class, panel::getUid);
    }

    @Test
    void reportsRejectedSegmentsWhenQueueIsFull() {
        SegmentPanel panel = new SegmentPanel("biz", 1, 50, List.of(new Segment(1, 100)), null);

        assertTrue(panel.addSegment(new Segment(100, 100)));
        assertFalse(panel.addSegment(new Segment(200, 100)));
    }

    private static List<Segment> segments(AtomicLong nextStart, int segmentNum) {
        return java.util.stream.IntStream.range(0, segmentNum)
            .mapToObj(index -> new Segment(nextStart.getAndAdd(100), 100))
            .toList();
    }
}
