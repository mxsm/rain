package com.github.mxsm.rain.uid.client.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxsm.rain.uid.client.Config;
import com.github.mxsm.rain.uid.client.Http2Requester;

import com.github.mxsm.rain.uid.core.SegmentUidGenerator;
import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.segment.AbstractSegmentUidGenerator;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentConsumerListener;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mxsm
 * @date 2022/4/30 21:05
 * @Since 1.0.0
 */
public class SegmentUidGeneratorClientImpl extends AbstractSegmentUidGenerator implements SegmentUidGenerator,
    SegmentConsumerListener {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String SEGMENT_UID_PATH = "/api/v1/segment/uid/";

    public static final String SEGMENTS_PATH = "/api/v1/segment/list/";

    private String uidGeneratorServerUir;

    private Config config;

    private int threshold;

    private ExecutorService executorService;

    public SegmentUidGeneratorClientImpl(final Config config) {
        super(config.getSegmentNum());
        this.config = config;
        this.uidGeneratorServerUir = config.getUidGeneratorServerUir();
        this.threshold = config.getThreshold();
        this.executorService = Executors.newFixedThreadPool(Math.max(1, config.getPrefetchThreads()),
            new PrefetchThreadFactory());
    }

    @Override
    public SegmentPanel createSegmentPanel(String bizCode, int stepSize) {
        List<Segment> segments = getSegments(bizCode, stepSize);
        return new SegmentPanel(bizCode, stepSize, threshold, segments, this);

    }

    /**
     * get step of number steps
     *
     * @param bizCode
     * @param segmentNum
     * @return
     */
    @Override
    public List<Segment> getSegments(String bizCode, int segmentNum) {
        try {
            StringBuilder path = new StringBuilder(SEGMENTS_PATH).append(bizCode);
            HashMap<String, String> params = new HashMap<>();
            params.put("segmentNum", String.valueOf(segmentNum));
            String content = Http2Requester.executeGET(config, path.toString(), params);
            Result<List<Segment>> result = OBJECT_MAPPER.readValue(content, new TypeReference<>() {
            });
            if (!result.isSuccess()) {
                throw new UidGenerateException(ErrorCode.UPSTREAM_UNAVAILABLE,
                    "Segment server returned " + result.getCode() + ": " + result.getMsg());
            }
            return result.getData();
        } catch (Exception e) {
            if (e instanceof UidGenerateException uidGenerateException) {
                throw uidGenerateException;
            }
            throw new UidGenerateException(ErrorCode.UPSTREAM_UNAVAILABLE,
                "Get segments from remote [URL=" + this.uidGeneratorServerUir + "] error", e);
        }
    }

    @Override
    public void listener(SegmentPanel segmentPanel, int segmentSize) {
        AsyncHandleTask task = new AsyncHandleTask(segmentPanel, segmentSize);
        executorService.submit(task);
    }

    /**
     * Get a unique ID for segment
     *
     * @param bizCode
     * @return UID
     * @throws UidGenerateException
     */
    @Override
    public long getUID(String bizCode) throws UidGenerateException {
        StringBuilder path = new StringBuilder(SEGMENT_UID_PATH).append(bizCode);
        try {
            String content = Http2Requester.executeGET(config, path.toString());
            Result<Long> result = OBJECT_MAPPER.readValue(content, new TypeReference<>() {
            });
            if (!result.isSuccess()) {
                throw new UidGenerateException(ErrorCode.UPSTREAM_UNAVAILABLE,
                    "Segment server returned " + result.getCode() + ": " + result.getMsg());
            }
            return result.getData();
        } catch (Exception e) {
            if (e instanceof UidGenerateException uidGenerateException) {
                throw uidGenerateException;
            }
            throw new UidGenerateException("Get Uid from remote [URL=" + this.uidGeneratorServerUir + path + "] error",
                e);
        }
    }

    public long getUIDFromLocalCache(String bizCode) throws UidGenerateException {
        return super.getUID(bizCode);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    class AsyncHandleTask implements Runnable {

        private SegmentPanel segmentPanel;

        private int segmentSize;

        public AsyncHandleTask(SegmentPanel segmentPanel, int segmentSize) {

            this.segmentPanel = segmentPanel;
            this.segmentSize = segmentSize;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
         * causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                String bizCode = segmentPanel.getBizCode();
                List<Segment> segments = getSegments(bizCode, segmentSize);
                segmentPanel.addSegment(segments);
                segmentPanel.resetCounter();
            } finally {
                segmentPanel.refillFinished();
            }
        }
    }

    private static class PrefetchThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "rain-segment-prefetch-" + threadNum.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
