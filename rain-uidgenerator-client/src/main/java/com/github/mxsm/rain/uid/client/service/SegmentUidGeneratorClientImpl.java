package com.github.mxsm.rain.uid.client.service;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.mxsm.rain.uid.client.Http2Requester;
import com.github.mxsm.rain.uid.client.utils.UrlUtils;

import com.github.mxsm.rain.uid.core.SegmentUidGenerator;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.segment.AbstractSegmentUidGenerator;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.core.segment.SegmentConsumerListener;
import com.github.mxsm.rain.uid.core.segment.SegmentPanel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author mxsm
 * @date 2022/4/30 21:05
 * @Since 1.0.0
 */
public class SegmentUidGeneratorClientImpl extends AbstractSegmentUidGenerator implements SegmentUidGenerator,
    SegmentConsumerListener {

    public static final String SEGMENT_UID_PATH = "/api/v1/segment/uid/";

    public static final String SEGMENTS_PATH = "/api/v1/segment/list/";

    private String uidGeneratorServerUir;

    private int threshold;

    private ExecutorService executorService;

    private String host;

    private int port;

    public SegmentUidGeneratorClientImpl(String uidGeneratorServerUir, int cacheSize, int threshold,
        ExecutorService executorService) {
        super(cacheSize);
        this.uidGeneratorServerUir = uidGeneratorServerUir;
        this.threshold = threshold;
        this.executorService = executorService;
        parseURL();
    }

    private void parseURL() {
        String[] sts = UrlUtils.parseUriAndPort(this.uidGeneratorServerUir);
        this.host = sts[0];
        this.port = Integer.parseInt(sts[1]);
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
            String content = Http2Requester.executeGET(host, port, path.toString(), new HashMap<>());
            Result<List<Segment>> result = JSON.parseObject(content, new TypeReference<>() {
            });
            return result.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void listener(SegmentPanel segmentPanel, int segmentSize) {
        AsyncHandleTask task = new AsyncHandleTask(segmentPanel, segmentSize);
        if (executorService == null) {
            task.run();
            return;
        }
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
            String content = Http2Requester.executeGET(host, port, path.toString());
            return Long.parseLong(content);
        } catch (Exception e) {
            throw new UidGenerateException("Get Uid from remote [URL=" + this.uidGeneratorServerUir + path + "] error",
                e);
        }
    }

    public long getUIDFromLocalCache(String bizCode) throws UidGenerateException {
        return super.getUID(bizCode);
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
            String bizCode = segmentPanel.getBizCode();
            List<Segment> segments = getSegments(bizCode, segmentSize);
            segmentPanel.addSegment(segments);
            segmentPanel.resetCounter();
        }
    }
}
