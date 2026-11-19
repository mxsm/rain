package com.github.mxsm.rain.uid.service;

import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.dao.AllocationDao;
import com.github.mxsm.rain.uid.entity.AllocationEntity;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * @author mxsm
 * @date 2022/4/30 7:19
 * @Since 1.0.0
 */
@Service("allocationServiceImpl")
public class AllocationServiceImpl implements AllocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllocationServiceImpl.class);

    private final AllocationDao allocationDao;

    private final UidMetrics uidMetrics;

    public AllocationServiceImpl(AllocationDao allocationDao, UidMetrics uidMetrics) {
        this.allocationDao = allocationDao;
        this.uidMetrics = uidMetrics;
    }

    @Override
    public List<Segment> getSegments(String bizCode, int segmentNum) {
        long start = System.nanoTime();
        validateSegmentRequest(bizCode, segmentNum);
        AllocationEntity allocation;
        try {
            allocation = allocationDao.reserve(bizCode, segmentNum);
        } catch (DataAccessException ex) {
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.SEGMENT_ALLOCATE_FAILED,
                "Failed to reserve segments for bizCode " + bizCode, ex);
        }
        if (allocation == null) {
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.BIZ_CODE_NOT_FOUND, "bizCode not registered: " + bizCode);
        }
        Integer stepLength = allocation.getStep();
        if (stepLength == null || stepLength <= 0) {
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.SEGMENT_ALLOCATE_FAILED,
                "Invalid step for bizCode " + bizCode);
        }
        long totalLength = (long) stepLength * segmentNum;
        long startUid = Math.subtractExact(allocation.getMaxId(), totalLength);
        List<Segment> segments = new ArrayList<>(segmentNum);
        for (int index = 0; index < segmentNum; index++) {
            segments.add(new Segment(startUid + (long) stepLength * index, stepLength));
        }
        uidMetrics.recordSegmentAllocation(System.nanoTime() - start);
        return segments;
    }

    @Override
    public boolean registerBizCode(String bizCode, int step) {
        if (StringUtils.isBlank(bizCode)) {
            throw new UidGenerateException(ErrorCode.VALIDATION_ERROR, "bizCode must not be blank");
        }
        if (step <= 0) {
            throw new UidGenerateException(ErrorCode.VALIDATION_ERROR, "step must be greater than 0");
        }
        try {
            allocationDao.register(bizCode, step);
        } catch (Exception e) {
            LOGGER.error("register biz code error",e);
            throw e;
        }
        return true;
    }

    private void validateSegmentRequest(String bizCode, int segmentNum) {
        if (StringUtils.isBlank(bizCode)) {
            throw new UidGenerateException(ErrorCode.VALIDATION_ERROR, "bizCode must not be blank");
        }
        if (segmentNum <= 0 || segmentNum > 10000) {
            throw new UidGenerateException(ErrorCode.VALIDATION_ERROR,
                "segmentNum must be between 1 and 10000");
        }
    }
}
