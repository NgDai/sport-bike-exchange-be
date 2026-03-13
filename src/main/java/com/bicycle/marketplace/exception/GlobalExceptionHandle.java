package com.bicycle.marketplace.exception;

import com.bicycle.marketplace.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleException(AppException exception) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var fieldErrors = exception.getBindingResult().getFieldErrors();
        StringBuilder messageBuilder = new StringBuilder();
        int code = ErrorCode.INVALID_KEY.getCode();
        for (var fieldError : fieldErrors) {
            String enumKey = fieldError.getDefaultMessage();
            try {
                ErrorCode errorCode = ErrorCode.valueOf(enumKey);
                code = errorCode.getCode();
                if (messageBuilder.length() > 0) messageBuilder.append("; ");
                messageBuilder.append(errorCode.getMessage());
            } catch (IllegalArgumentException e) {
                if (messageBuilder.length() > 0) messageBuilder.append("; ");
                messageBuilder.append(fieldError.getField()).append(": ").append(enumKey);
            }
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(code);
        apiResponse.setMessage(messageBuilder.length() > 0 ? messageBuilder.toString() : ErrorCode.INVALID_KEY.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
