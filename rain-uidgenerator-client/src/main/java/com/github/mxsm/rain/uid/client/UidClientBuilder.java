package com.github.mxsm.rain.uid.client;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * @author mxsm
 * @date 2022/4/30 15:57
 * @Since 1.0.0
 */
public final class UidClientBuilder {

    private Config config = new Config();

    /**
     * @deprecated use {@link #setUidGeneratorServerUris(Collection)} for multi-endpoint failover.
     */
    @Deprecated(since = "1.0.1", forRemoval = false)
    public UidClientBuilder setUidGeneratorServerUir(String uidGeneratorServerUir) {
        this.config.setUidGeneratorServerUir(uidGeneratorServerUir);
        return this;
    }

    public UidClientBuilder setUidGeneratorServerUris(Collection<String> uidGeneratorServerUris) {
        this.config.setUidGeneratorServerUris(uidGeneratorServerUris);
        return this;
    }

    public UidClientBuilder setUidGeneratorServerUris(String... uidGeneratorServerUris) {
        this.config.setUidGeneratorServerUris(List.of(uidGeneratorServerUris));
        return this;
    }

    public UidClientBuilder setToken(String token) {
        this.config.setToken(token);
        return this;
    }

    public UidClientBuilder setConnectTimeout(Duration connectTimeout) {
        this.config.setConnectTimeout(connectTimeout);
        return this;
    }

    public UidClientBuilder setReadTimeout(Duration readTimeout) {
        this.config.setReadTimeout(readTimeout);
        return this;
    }

    public UidClientBuilder setMaxRetries(int maxRetries) {
        this.config.setMaxRetries(maxRetries);
        return this;
    }

    public UidClientBuilder setPrefetchThreads(int prefetchThreads) {
        this.config.setPrefetchThreads(prefetchThreads);
        return this;
    }

    public UidClientBuilder setSegmentNum(int segmentNum) {
        this.config.setSegmentNum(segmentNum);
        return this;
    }

    public UidClientBuilder setThreshold(int threshold) {
        this.config.setThreshold(threshold);
        return this;
    }


    public UidClientBuilder setTimestampBits(int timestampBits) {
        this.config.setTimestampBits(timestampBits);
        return this;
    }

    public UidClientBuilder setMachineIdBits(int machineIdBits) {
        this.config.setMachineIdBits(machineIdBits);
        return this;
    }


    public UidClientBuilder setSequenceBits(int sequenceBits) {
        this.config.setSequenceBits(sequenceBits);
        return this;
    }

    public UidClientBuilder isTimeBitsSecond(boolean timeBitsSecond) {
        this.config.setTimeBitsSecond(timeBitsSecond);
        return this;
    }

    public UidClientBuilder setEpoch(String epoch) {
        this.config.setEpoch(epoch);
        return this;
    }

    public UidClientBuilder isSnowflakeUidFromRemote(boolean fromRemote) {
        this.config.setSnowflakeUidFromRemote(fromRemote);
        return this;
    }

    public UidClientBuilder isSegmentUidFromRemote(boolean fromRemote) {
        this.config.setSegmentUidFromRemote(fromRemote);
        return this;
    }



    public UidClient build() {
        return new UidClientImpl(config);
    }

}
