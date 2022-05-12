package com.github.mxsm.rain.uid.controller;


import com.github.mxsm.rain.uid.core.SnowflakeUidGenerator;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.common.SnowflakeUidParsedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mxsm
 * @date 2022/5/1 17:28
 * @Since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/snowflake")
public class SnowflakeUidGeneratorController {

    @Autowired
    private SnowflakeUidGenerator snowflakeUidGenerator;

    @GetMapping("/uid")
    public long getUid() {
        return snowflakeUidGenerator.getUID();
    }

    @GetMapping("/parse/{uid}")
    public Result<SnowflakeUidParsedResult> getUid(@PathVariable("uid") Long uid) {
        return Result.buildSuccess(snowflakeUidGenerator.parseUID(uid));
    }

}
