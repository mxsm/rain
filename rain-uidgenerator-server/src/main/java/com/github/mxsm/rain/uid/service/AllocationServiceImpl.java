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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mxsm
 * @date 2022/4/30 7:19
 * @Since 1.0.0
 */
@Service("allocationServiceImpl")
public class AllocationServiceImpl implements AllocationService{

    private static final Logger LOGGER = LoggerFactory.getLogger(AllocationServiceImpl.class);

    @Autowired
    private AllocationDao allocationDao;

    @Autowired
    private UidMetrics uidMetrics;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Segment> getSegments(String bizCode, int segmentNum) {
        long start = System.nanoTime();
        validateSegmentRequest(bizCode, segmentNum);
        AllocationEntity allocation = allocationDao.getAllocation(bizCode);
        if(allocation == null){
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.BIZ_CODE_NOT_FOUND, "bizCode not registered: " + bizCode);
        }
        Integer stepLength = allocation.getStep();
        long totalLength = Math.multiplyExact(stepLength.longValue(), segmentNum);
        int updated = allocationDao.updateAllocation(segmentNum, bizCode);
        if (updated != 1) {
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.SEGMENT_ALLOCATE_FAILED,
                "Failed to allocate segment for bizCode " + bizCode);
        }
        Long newMaxId = allocationDao.getLastInsertId();
        if (newMaxId == null) {
            uidMetrics.recordSegmentAllocationFailure();
            throw new UidGenerateException(ErrorCode.SEGMENT_ALLOCATE_FAILED,
                "Failed to read allocated segment cursor for bizCode " + bizCode);
        }
        long startUid = Math.subtractExact(newMaxId, totalLength);
        List<Segment> segments = new ArrayList<>();
        for(int index = 0; index < segmentNum; ++ index){
            Segment segment = new Segment(startUid + stepLength.longValue() * index, stepLength);
            segments.add(segment);
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
            AllocationEntity entity = new AllocationEntity();
            entity.setStep(step);
            entity.setBizCode(bizCode);
            allocationDao.insertAllocation(entity);
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
