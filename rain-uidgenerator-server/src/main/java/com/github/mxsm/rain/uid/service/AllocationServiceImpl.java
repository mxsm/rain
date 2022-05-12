package com.github.mxsm.rain.uid.service;

import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.dao.AllocationDao;
import com.github.mxsm.rain.uid.entity.AllocationEntity;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Segment> getSegments(String bizCode, int segmentNum) {
        AllocationEntity allocation = allocationDao.getAllocation(bizCode);
        if(allocation == null){
            return new ArrayList<>();
        }
        allocationDao.updateAllocation(segmentNum, bizCode);
        Long startUid = allocation.getMaxId();
        Integer stepLength = allocation.getStep();
        List<Segment> segments = new ArrayList<>();
        for(int index = 0; index < segmentNum; ++ index){
            Segment segment = new Segment(startUid + stepLength * index, stepLength);
            segments.add(segment);
        }
        return segments;
    }

    @Override
    public boolean registerBizCode(String bizCode, int step) {
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
}
