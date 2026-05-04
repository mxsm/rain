package com.github.mxsm.rain.uid.client;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mxsm
 * @date 2022/5/3 17:00
 * @Since 1.0.0
 */
public class Config {

    //server url
    private String uidGeneratorServerUir;

    private List<URI> uidGeneratorServerUris = new ArrayList<>();

    private final AtomicInteger endpointCursor = new AtomicInteger();

    private String token;

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration readTimeout = Duration.ofSeconds(5);

    private int maxRetries = 1;

    private int prefetchThreads = 2;

    //get segment number from remote server
    private int segmentNum = 16;

    //threshold of get segment from remote
    private int threshold = 30;

    //bit‘s length of snowflake timestamp
    private int timestampBits = 41;

    //bit‘s length of  snowflake machine id
    private int machineIdBits = 10;

    //bit‘s length of  snowflake sequence
    private int sequenceBits = 12;

    //setting timestamp is second or millisecond
    private boolean timeBitsSecond = false;

    // start epoch, and must before now
    private String epoch = "2015-05-01";

    //Whether to obtain the snowflake ID remotely or locally
    private boolean snowflakeUidFromRemote = true;

    //Whether to obtain the segment ID remotely or locally
    private boolean segmentUidFromRemote = true;



    public int getSegmentNum() {
        return segmentNum;
    }

    public void setSegmentNum(int segmentNum) {
        this.segmentNum = segmentNum;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public void setTimestampBits(int timestampBits) {
        this.timestampBits = timestampBits;
    }

    public int getMachineIdBits() {
        return machineIdBits;
    }

    public void setMachineIdBits(int machineIdBits) {
        this.machineIdBits = machineIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public void setSequenceBits(int sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public boolean isTimeBitsSecond() {
        return timeBitsSecond;
    }

    public void setTimeBitsSecond(boolean timeBitsSecond) {
        this.timeBitsSecond = timeBitsSecond;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public String getUidGeneratorServerUir() {
        return uidGeneratorServerUir;
    }

    public void setUidGeneratorServerUir(String uidGeneratorServerUir) {
        this.uidGeneratorServerUir = uidGeneratorServerUir;
        if (uidGeneratorServerUir != null && !uidGeneratorServerUir.isBlank()) {
            this.uidGeneratorServerUris = List.of(normalizeUri(uidGeneratorServerUir));
        }
    }

    public List<URI> getUidGeneratorServerUris() {
        return uidGeneratorServerUris;
    }

    int nextEndpointIndex(int endpointSize) {
        if (endpointSize <= 0) {
            return 0;
        }
        return Math.floorMod(endpointCursor.getAndIncrement(), endpointSize);
    }

    public void setUidGeneratorServerUris(Collection<String> uidGeneratorServerUris) {
        this.uidGeneratorServerUris = new ArrayList<>();
        if (uidGeneratorServerUris == null) {
            return;
        }
        for (String uri : uidGeneratorServerUris) {
            if (uri != null && !uri.isBlank()) {
                this.uidGeneratorServerUris.add(normalizeUri(uri));
            }
        }
        this.uidGeneratorServerUir = this.uidGeneratorServerUris.isEmpty() ? null
            : this.uidGeneratorServerUris.getFirst().toString();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getPrefetchThreads() {
        return prefetchThreads;
    }

    public void setPrefetchThreads(int prefetchThreads) {
        this.prefetchThreads = prefetchThreads;
    }

    public boolean isSnowflakeUidFromRemote() {
        return snowflakeUidFromRemote;
    }

    public void setSnowflakeUidFromRemote(boolean snowflakeUidFromRemote) {
        this.snowflakeUidFromRemote = snowflakeUidFromRemote;
    }

    public boolean isSegmentUidFromRemote() {
        return segmentUidFromRemote;
    }

    public void setSegmentUidFromRemote(boolean segmentUidFromRemote) {
        this.segmentUidFromRemote = segmentUidFromRemote;
    }

    private URI normalizeUri(String value) {
        String normalized = value.contains("://") ? value : "http://" + value;
        URI uri = URI.create(normalized);
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return uri;
        }
        return URI.create(uri.toString().replaceAll("/+$", ""));
    }
}
