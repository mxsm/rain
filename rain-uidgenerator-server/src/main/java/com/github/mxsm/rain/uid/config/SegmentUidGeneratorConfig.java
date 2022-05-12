package com.github.mxsm.rain.uid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author mxsm
 * @date 2022/4/30 7:48
 * @Since 1.0.0
 */
@ConfigurationProperties(prefix = "mxsm.uid.segment")
@Configuration
public class SegmentUidGeneratorConfig {

    private int threshold;

    private int cacheSize;

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
