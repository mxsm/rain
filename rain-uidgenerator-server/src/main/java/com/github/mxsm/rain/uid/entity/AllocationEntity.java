package com.github.mxsm.rain.uid.entity;

import java.time.LocalDateTime;

/**
 * @author mxsm
 * @date 2022/4/17 17:01
 * @Since 1.0.0
 */
public class AllocationEntity {

    private Long id;

    private String bizCode;

    private Long maxId;

    private Integer step;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public AllocationEntity(String bizCode) {
        this.bizCode = bizCode;
    }

    public AllocationEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
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
