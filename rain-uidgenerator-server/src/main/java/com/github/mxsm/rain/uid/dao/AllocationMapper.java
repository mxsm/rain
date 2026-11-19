package com.github.mxsm.rain.uid.dao;

import com.github.mxsm.rain.uid.entity.AllocationEntity;
import org.apache.ibatis.annotations.Param;

interface AllocationMapper {

    void insertAllocation(@Param("alloc") AllocationEntity allocation);

    AllocationEntity lockAllocation(@Param("bizCode") String bizCode);

    void advanceLockedAllocation(@Param("alloc") AllocationEntity allocation, @Param("segmentNum") int segmentNum);

    AllocationEntity reserveReturning(@Param("bizCode") String bizCode, @Param("segmentNum") int segmentNum);
}
