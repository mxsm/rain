package com.github.mxsm.rain.uid.entity;

import com.github.mxsm.rain.uid.common.DeployEnvType;
import java.time.LocalDateTime;

/**
 * @author mxsm
 * @date 2022/5/1 21:13
 * @Since 1.0.0
 */
public class SnowflakeNodeEntity {

    private Long id;

    private Long hostName;

    private Integer port;

    private DeployEnvType deployEnvType;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostName() {
        return hostName;
    }

    public void setHostName(Long hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public DeployEnvType getDeployEnvType() {
        return deployEnvType;
    }

    public void setDeployEnvType(DeployEnvType deployEnvType) {
        this.deployEnvType = deployEnvType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
