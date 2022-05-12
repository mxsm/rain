package com.github.mxsm.rain.uid.dao;

import com.github.mxsm.rain.uid.entity.SnowflakeNodeEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author mxsm
 * @date 2022/5/1 21:21
 * @Since 1.0.0
 */
public interface SnowflakeNodeDao {

    SnowflakeNodeEntity selectSnowflakeNode(@Param("hostName") long hostName, @Param("port") int port);

    void insertSnowflakeNode(@Param("sf") SnowflakeNodeEntity sf);

}
