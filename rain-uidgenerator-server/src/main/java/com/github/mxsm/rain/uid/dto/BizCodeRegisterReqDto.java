package com.github.mxsm.rain.uid.dto;


import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author mxsm
 * @date 2022/5/6 9:45
 * @Since 1.0.0
 */
@Valid
public class BizCodeRegisterReqDto {

    @NotEmpty
    private String bizCode;

    @NotNull
    private Integer step;

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}
