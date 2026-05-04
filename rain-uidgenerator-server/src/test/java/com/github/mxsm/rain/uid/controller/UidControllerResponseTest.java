package com.github.mxsm.rain.uid.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mxsm.rain.uid.core.SegmentUidGenerator;
import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import com.github.mxsm.rain.uid.service.AllocationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UidControllerResponseTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UidMetrics metrics = new UidMetrics(new SimpleMeterRegistry());
        SegmentUidGeneratorController segmentController = new SegmentUidGeneratorController(allocationService(),
            segmentUidGenerator(), metrics);
        SnowflakeUidGeneratorController snowflakeController = new SnowflakeUidGeneratorController(
            snowflakeUidGenerator(), metrics);

        mockMvc = MockMvcBuilders.standaloneSetup(segmentController, snowflakeController).build();
    }

    @Test
    void segmentUidReturnsResultEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/segment/uid/biz"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    void snowflakeUidReturnsResultEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/snowflake/uid"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data").value(200));
    }

    @Test
    void deprecatedGetUidStillWorks() throws Exception {
        mockMvc.perform(get("/api/v1/snowflake/uid"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(200));
    }

    private AllocationService allocationService() {
        return new AllocationService() {
            @Override
            public List<Segment> getSegments(String bizCode, int segmentNum) {
                return List.of(new Segment(1, 10));
            }

            @Override
            public boolean registerBizCode(String bizCode, int step) {
                return true;
            }
        };
    }

    private SegmentUidGenerator segmentUidGenerator() {
        return new SegmentUidGenerator() {
            @Override
            public long getUID(String bizCode) {
                return 100;
            }

            @Override
            public List<Segment> getSegments(String bizCode, int segmentNum) {
                return List.of(new Segment(1, 10));
            }
        };
    }

    private SnowflakeUidGenerator snowflakeUidGenerator() {
        return new SnowflakeUidGenerator() {
            @Override
            public long getUID() {
                return 200;
            }

            @Override
            public SnowflakeUidParsedResult parseUID(long uid) {
                return new SnowflakeUidParsedResult(uid, "2026-01-01 00:00:00", 1, 1);
            }
        };
    }
}
