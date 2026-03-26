package com.example.demo.common;

public enum ResultCode {

    // 基础状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统繁忙，请稍后再试"),

    // 权限相关
    TOKEN_INVALID(401, "登录凭证已缺失或过期，请重新登录"),
    TOKEN_MISSING(401, "非法操作：敏感动作需要 Token"),

    // 用户相关业务状态码
    USER_NOT_FOUND(404, "用户不存在"),
    USER_CREATE_FAILED(500, "用户创建失败"),
    USER_UPDATE_FAILED(500, "用户更新失败"),
    USER_DELETE_FAILED(500, "用户删除失败"),

    // 注册与登录相关业务状态码
    USER_HAS_EXISTED(4001, "该用户名已被注册"),
    USER_NOT_EXIST(4002, "该用户不存在"),
    PASSWORD_ERROR(4003, "账号或密码错误");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}