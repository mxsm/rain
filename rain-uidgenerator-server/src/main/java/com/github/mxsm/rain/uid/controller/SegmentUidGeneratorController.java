package com.github.mxsm.rain.uid.controller;

import com.github.mxsm.rain.uid.core.SegmentUidGenerator;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.dto.BizCodeRegisterReqDto;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import com.github.mxsm.rain.uid.service.AllocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Segment UID API.
 */
@RestController
@RequestMapping("/api/v1/segment")
@Validated
public class SegmentUidGeneratorController {

    private final AllocationService allocationService;

    private final SegmentUidGenerator segmentUidGenerator;

    private final UidMetrics uidMetrics;

    public SegmentUidGeneratorController(AllocationService allocationService, SegmentUidGenerator segmentUidGenerator,
        UidMetrics uidMetrics) {
        this.allocationService = allocationService;
        this.segmentUidGenerator = segmentUidGenerator;
        this.uidMetrics = uidMetrics;
    }

    @PostMapping("/rg")
    public Result<Boolean> registerBizCode(@RequestBody @Valid BizCodeRegisterReqDto params) {
        return Result.buildSuccess(allocationService.registerBizCode(params.getBizCode(), params.getStep()));
    }

    @PostMapping("/uid/{bizCode}")
    public Result<Long> createUid(@PathVariable("bizCode") @NotBlank String bizCode) {
        long uid = segmentUidGenerator.getUID(bizCode);
        uidMetrics.recordSegmentGenerated();
        return Result.buildSuccess(uid);
    }

    /**
     * @deprecated UID generation has side effects. Use POST /api/v1/segment/uid/{bizCode}.
     */
    @Deprecated(since = "1.0.1", forRemoval = false)
    @GetMapping("/uid/{bizCode}")
    public Result<Long> getUid(@PathVariable("bizCode") @NotBlank String bizCode) {
        return createUid(bizCode);
    }

    @GetMapping("/list/{bizCode}")
    public Result<List<Segment>> getStep(@PathVariable("bizCode") @NotBlank String bizCode,
        @RequestParam("segmentNum") @Min(1) @Max(10000) Integer segmentNum) {
        return Result.buildSuccess(allocationService.getSegments(bizCode, segmentNum));
    }
}
