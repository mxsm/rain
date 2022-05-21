package com.github.mxsm.rain.uid.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author mxsm
 * @date 2022/5/21 10:18
 * @Since 1.0.0
 */
class UidClientTest {

    private UidClient client;

    @BeforeEach
    void setUp() {
        client = UidClient.builder().setUidGeneratorServerUir("172.23.186.56:8080").setSegmentNum(16).isSegmentUidFromRemote(false).build();
    }

    @Test
    void getSegmentUid() {
        long segmentUid = client.getSegmentUid("uQG6n50NSIR6Fcuh19093632");
        assertTrue(segmentUid > 0);
    }

    @Test
    void testGetSegmentUid() {
    }

    @Test
    void getSnowflakeUid() {
    }

    @Test
    void parseSnowflakeUid() {
    }

    @Test
    void builder() {
    }
}