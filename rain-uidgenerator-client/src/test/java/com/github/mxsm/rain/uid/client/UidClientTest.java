package com.github.mxsm.rain.uid.client;

import static org.junit.jupiter.api.Assertions.*;

import com.github.mxsm.rain.uid.client.service.SegmentUidGeneratorClientImpl;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.core.segment.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * @author mxsm
 * @date 2022/5/21 10:18
 * @Since 1.0.0
 */
class UidClientTest {

    @Test
    void getSegmentUidReturnsConsecutiveLocalIds() {
        SegmentUidGeneratorClientImpl client = new LocalSegmentUidGeneratorClient(4, 20);

        long firstUid = client.getUIDFromLocalCache("test-biz-code");
        long secondUid = client.getUIDFromLocalCache("test-biz-code");
        long thirdUid = client.getUIDFromLocalCache("test-biz-code");

        assertEquals(firstUid + 1, secondUid);
        assertEquals(secondUid + 1, thirdUid);
        assertTrue(firstUid > 0);
    }

    @Test
    void getSegmentUidStartsFromFirstLocalSegment() {
        SegmentUidGeneratorClientImpl client = new LocalSegmentUidGeneratorClient(2, 50);

        assertEquals(1L, client.getUIDFromLocalCache("test-biz-code"));
        assertEquals(2L, client.getUIDFromLocalCache("test-biz-code"));
    }

    @Test
    void getSnowflakeUid() {
        UidClient client = localClient();
        try {
            long uid = client.getSnowflakeUid();

            assertTrue(uid > 0);
        } finally {
            client.shutdown();
        }
    }

    @Test
    void parseSnowflakeUid() {
        UidClient client = localClient();
        try {
            long uid = client.getSnowflakeUid();
            SnowflakeUidParsedResult result = client.parseSnowflakeUid(uid);

            assertEquals(uid, result.getUid());
            assertNotNull(result.getTimestamp());
            assertTrue(result.getMachineId() >= 0);
            assertTrue(result.getSequence() >= 0);
        } finally {
            client.shutdown();
        }
    }

    @Test
    void builder() {
        UidClient client = localClient();
        try {
            assertNotNull(client);
        } finally {
            client.shutdown();
        }
    }

    private UidClient localClient() {
        return UidClient.builder()
            .isSegmentUidFromRemote(false)
            .isSnowflakeUidFromRemote(false)
            .build();
    }

    private static class LocalSegmentUidGeneratorClient extends SegmentUidGeneratorClientImpl {

        private final AtomicLong nextSegmentStart = new AtomicLong(1);

        LocalSegmentUidGeneratorClient(int segmentNum, int threshold) {
            super(config(segmentNum, threshold));
        }

        @Override
        public List<Segment> getSegments(String bizCode, int segmentNum) {
            List<Segment> segments = new ArrayList<>(segmentNum);
            for (int i = 0; i < segmentNum; i++) {
                segments.add(new Segment(nextSegmentStart.getAndAdd(100), 100));
            }
            return segments;
        }

        private static Config config(int segmentNum, int threshold) {
            Config config = new Config();
            config.setSegmentNum(segmentNum);
            config.setThreshold(threshold);
            config.setSegmentUidFromRemote(false);
            config.setSnowflakeUidFromRemote(false);
            return config;
        }
    }
}
