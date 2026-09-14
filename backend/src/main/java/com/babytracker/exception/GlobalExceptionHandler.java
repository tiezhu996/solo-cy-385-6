package com.babytracker.exception;

import com.babytracker.constants.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BizException.class)
    public Map<String, Object> handleBiz(BizException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Map.of("success", false, "code", ex.getCode(), "message", ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handle(Exception ex) {
        log.error("服务器内部错误", ex);
        return Map.of("success", false, "code", ErrorCode.INTERNAL_ERROR, "message", "服务器内部错误");
    }
}
