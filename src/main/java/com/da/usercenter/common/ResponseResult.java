package com.da.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @author Da
 * &#064;date  2023/6/12 15:54
 */
@Data
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = -5570699418243385684L;
    private Integer code;
    private T data;
    private String message;
    private String description;

    public ResponseResult() {
    }

    public ResponseResult(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }


    /**
     * 成功
     * @param data 数据
     * @param message 详细信息
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> success(T data, String message){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.data = data;
        result.message = message;
        return result;
    }



    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> success(T data){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.data = data;
        return result;
    }

    /**
     * 成功
     * @param code
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> success(Integer code){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        return result;
    }


    /**
     * 失败
     * @param errorCode
     * @param data
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> error(ErrorCode errorCode, T data){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = errorCode.getCode();
        result.data = data;
        result.message = errorCode.getMessage();
        result.description = errorCode.getDescription();
        return result;
    }

    /**
     * 失败
     * @param errorCode 错误状态码
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> error(ErrorCode errorCode, String description){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = errorCode.getCode();
        result.message = errorCode.getMessage();
        result.description = description;
        return result;
    }

    /**
     * 错误
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> ResponseResult<T> error(ErrorCode errorCode){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = errorCode.getCode();
        result.message = errorCode.getMessage();
        return result;
    }

    public static <T> ResponseResult<T> error(ErrorCode errorCode, String message, String description){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = errorCode.getCode();
        result.message = message;
        result.description = description;
        return result;
    }

    public static <T> ResponseResult<T> error(Integer errorCode, String message, String description){
        ResponseResult<T> result = new ResponseResult<>();
        result.code = errorCode;
        result.message = message;
        result.description = description;
        return result;
    }



}
