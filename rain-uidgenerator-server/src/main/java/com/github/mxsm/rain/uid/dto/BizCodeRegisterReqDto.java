package com.github.mxsm.rain.uid.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * @author mxsm
 * @date 2022/5/6 9:45
 * @Since 1.0.0
 */
@Valid
public class BizCodeRegisterReqDto {

    @NotEmpty
    @Size(max = 128)
    private String bizCode;

    @NotNull
    @Positive
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
