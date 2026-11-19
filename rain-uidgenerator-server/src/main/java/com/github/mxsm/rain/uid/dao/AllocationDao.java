package com.github.mxsm.rain.uid.dao;

import com.github.mxsm.rain.uid.entity.AllocationEntity;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mxsm
 * @date 2022/4/17 17:00
 * @Since 1.0.0
 */
@Repository
public class AllocationDao {

    private final AllocationMapper mapper;
    private final boolean postgresql;

    public AllocationDao(AllocationMapper mapper, SqlSessionFactory sqlSessionFactory) {
        this.mapper = mapper;
        this.postgresql = "PostgreSQL".equals(sqlSessionFactory.getConfiguration().getDatabaseId());
    }

    public void register(String bizCode, int step) {
        AllocationEntity allocation = new AllocationEntity(bizCode);
        allocation.setStep(step);
        mapper.insertAllocation(allocation);
    }

    @Transactional
    public AllocationEntity reserve(String bizCode, int segmentNum) {
        if (postgresql) {
            return mapper.reserveReturning(bizCode, segmentNum);
        }
        AllocationEntity allocation = mapper.lockAllocation(bizCode);
        // Legacy MySQL schemas allow invalid steps. Leave their cursor intact for the service to report.
        if (allocation != null && allocation.getStep() > 0) {
            mapper.advanceLockedAllocation(allocation, segmentNum);
        }
        return allocation;
    }
}
