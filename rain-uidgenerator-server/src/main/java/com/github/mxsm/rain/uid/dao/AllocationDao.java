package com.github.mxsm.rain.uid.dao;

import com.github.mxsm.rain.uid.entity.AllocationEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author mxsm
 * @date 2022/4/17 17:00
 * @Since 1.0.0
 */
public interface AllocationDao {

    AllocationEntity getAllocation(@Param("bizCode") String bizCode);

    void insertAllocation(@Param("alloc") AllocationEntity alloc);

    void updateAllocation(@Param("stepLength") int stepLength, @Param("bizCode") String bizCode);
}
