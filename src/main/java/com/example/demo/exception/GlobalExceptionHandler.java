package com.example.demo.exception;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈信息便于调试
        // 使用 ResultCode.ERROR 返回统一的系统错误
        return Result.error(ResultCode.ERROR);
    }

    // 处理算术异常（如除以零）
    @ExceptionHandler(ArithmeticException.class)
    public Result<String> handleArithmeticException(ArithmeticException e) {
        return Result.error(ResultCode.ERROR); // 也可以自定义算术异常状态码
    }

    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointerException(NullPointerException e) {
        return Result.error(ResultCode.ERROR);
    }
}