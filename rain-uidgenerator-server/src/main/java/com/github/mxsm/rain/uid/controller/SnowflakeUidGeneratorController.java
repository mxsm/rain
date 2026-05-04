package com.github.mxsm.rain.uid.controller;

import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Snowflake UID API.
 */
@RestController
@RequestMapping("/api/v1/snowflake")
public class SnowflakeUidGeneratorController {

    private final SnowflakeUidGenerator snowflakeUidGenerator;

    private final UidMetrics uidMetrics;

    public SnowflakeUidGeneratorController(SnowflakeUidGenerator snowflakeUidGenerator, UidMetrics uidMetrics) {
        this.snowflakeUidGenerator = snowflakeUidGenerator;
        this.uidMetrics = uidMetrics;
    }

    @PostMapping("/uid")
    public Result<Long> createUid() {
        long uid = snowflakeUidGenerator.getUID();
        uidMetrics.recordSnowflakeGenerated();
        return Result.buildSuccess(uid);
    }

    /**
     * @deprecated UID generation has side effects. Use POST /api/v1/snowflake/uid.
     */
    @Deprecated(since = "1.0.1", forRemoval = false)
    @GetMapping("/uid")
    public Result<Long> getUid() {
        return createUid();
    }

    @GetMapping("/parse/{uid}")
    public Result<SnowflakeUidParsedResult> parseUid(@PathVariable("uid") Long uid) {
        return Result.buildSuccess(snowflakeUidGenerator.parseUID(uid));
    }
}
