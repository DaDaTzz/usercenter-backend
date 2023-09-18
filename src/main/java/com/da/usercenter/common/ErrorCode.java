package com.da.usercenter.common;

/**
 * @author Da
 * @date 2023/6/12 16:00
 */

/**
 * 错误码
 */
public enum ErrorCode {

    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求参数为空", ""),
    NO_AUTH(40002, "没有权限", ""),
    NOT_LOGIN(40003, "未登录", ""),
    DATABASE_ERROR(40004,"保存异常",""),
    USER_STATE_ERROR(40005, "账号封禁", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),

    ;


    private final Integer code;
    private final String message;
    private final String description;


    ErrorCode(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
