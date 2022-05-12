package com.github.mxsm.rain.uid.core.common;

/**
 * @author mxsm
 * @date 2022/4/17 16:20
 * @Since 1.0.0
 */
public class Result<T> {

    private T data;

    private Status status;

    private String msg;

    public T getData() {
        return data;
    }

    public Result(T data, Status status, String msg) {
        this.data = data;
        this.status = status;
        this.msg = msg;
    }

    public Result() {

    }

    public void setData(T data) {
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Result{" +
            "data=" + data +
            ", status=" + status.name() +
            '}';
    }

    public static <T> Result<T> buildSuccess(T data) {

        Result<T> result = new Result<>();
        result.setStatus(Status.SUCCESS);
        result.setData(data);
        result.setMsg("SUCCESS");
        return result;
    }

    public static <T> Result<T> buildError(T data, String msg) {

        Result<T> result = new Result<>();
        result.setStatus(Status.EXCEPTION);
        result.setData(data);
        result.setMsg(msg);

        return result;
    }
}
