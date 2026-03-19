package com.example.demo.common;

public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    // 原有的 success 方法（只有数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.msg = ResultCode.SUCCESS.getMsg();
        result.data = data;
        return result;
    }

    // 新增：带自定义消息的 success 方法（兼容你的原有代码）
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.msg = message;  // 使用自定义消息
        result.data = data;
        return result;
    }

    // 原有的 error 方法（使用 ResultCode）
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.msg = resultCode.getMsg();
        result.data = null;
        return result;
    }

    // 新增：带自定义消息的 error 方法（兼容原有代码）
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.ERROR.getCode();
        result.msg = message;
        result.data = null;
        return result;
    }

    // Getter 和 Setter 方法
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}