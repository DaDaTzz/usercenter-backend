package com.da.usercenter.exception;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author Da
 * @date 2023/6/12 17:10
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult businessException(BusinessException e){
        log.error("businessException:" + e.getMessage(), e);
        return ResponseResult.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult runTimeException(BusinessException e){
        log.error("runTimeException:" + e);
        return ResponseResult.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getDescription());
    }
}
