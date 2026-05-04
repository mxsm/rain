package com.github.mxsm.rain.uid.controller;

import com.github.mxsm.rain.uid.core.SegmentUidGenerator;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.dto.BizCodeRegisterReqDto;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import com.github.mxsm.rain.uid.service.AllocationService;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mxsm
 * @date 2022/4/17 16:07
 * @Since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/segment")
@Validated
public class SegmentUidGeneratorController {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private SegmentUidGenerator segmentUidGenerator;

    @Autowired
    private UidMetrics uidMetrics;

    @PostMapping("/rg")
    public Result<Boolean> registerBizCode(@RequestBody @Valid BizCodeRegisterReqDto params){
        String bizCode = params.getBizCode();
        Integer step = params.getStep();
        return Result.buildSuccess(allocationService.registerBizCode(bizCode,step));
    }

    /**
     * get uid by bizcode
     *
     * @param bizCode
     * @return
     */
    @GetMapping("/uid/{bizCode}")
    public Result<Long> getUid(@PathVariable("bizCode") @NotBlank String bizCode) {
        long uid = segmentUidGenerator.getUID(bizCode);
        uidMetrics.recordSegmentGenerated();
        return Result.buildSuccess(uid);
    }

    /**
     * get bizcode step
     *
     * @param bizCode
     * @param segmentNum number of step
     * @return
     */
    @GetMapping("/list/{bizCode}")
    public Result<List<Segment>> getStep(@PathVariable("bizCode") @NotBlank String bizCode,
        @RequestParam("segmentNum") @Min(1) @Max(10000) Integer segmentNum) {
        List<Segment> segments = allocationService.getSegments(bizCode, segmentNum);
        return Result.buildSuccess(segments);
    }

}
