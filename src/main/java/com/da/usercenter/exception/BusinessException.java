package com.da.usercenter.exception;

import com.da.usercenter.common.ErrorCode;

/**
 * 自定义异常类
 * @author Da
 * @date 2023/6/12 16:44
 */
public class BusinessException extends RuntimeException{
    private final Integer code;
    private final String description;

    public BusinessException(String message, Integer code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
