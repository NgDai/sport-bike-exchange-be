package com.bicycle.marketplace.exception;

import com.bicycle.marketplace.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.UnexpectedRollbackException;

import jakarta.persistence.PersistenceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle {

    /** Bắt mọi exception chưa xử lý (tránh trả 500 + stack trace), trả JSON thống nhất */
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleException(Exception exception) {
        // Nếu exception bọc AppException (vd: throw từ lambda), trả về response 400 tương ứng
        Throwable cause = exception.getCause();
        if (cause instanceof AppException appEx) {
            return handleAppException(appEx);
        }
        log.error("Unhandled exception: {}", exception.getMessage(), exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.status(500).body(apiResponse);
    }

    private static boolean isTransactionSaveError(Throwable t) {
        if (t == null) return false;
        String msg = t.getMessage();
        if (msg == null) msg = "";
        String lower = msg.toLowerCase();
        return lower.contains("transaction") || lower.contains("constraint") || lower.contains("null value in column");
    }

    /** Lỗi ràng buộc DB (NOT NULL, FK, unique...) → trả 400; nếu liên quan transaction thì trả 1031 + hướng dẫn chạy SQL */
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        if (isTransactionSaveError(exception) || isTransactionSaveError(exception.getCause())) {
            apiResponse.setCode(ErrorCode.TRANSACTION_SAVE_FAILED.getCode());
            apiResponse.setMessage(ErrorCode.TRANSACTION_SAVE_FAILED.getMessage());
        } else {
            apiResponse.setCode(ErrorCode.INVALID_REQUEST.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_REQUEST.getMessage());
        }
        return ResponseEntity.badRequest().body(apiResponse);
    }

    /** Lỗi JPA/Hibernate (persist, flush...) → trả 400; nếu liên quan transaction thì trả 1031 + hướng dẫn chạy SQL */
    @ExceptionHandler(value = { PersistenceException.class, JpaSystemException.class })
    ResponseEntity<ApiResponse> handlePersistence(Exception exception) {
        log.warn("Persistence error: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        if (isTransactionSaveError(exception) || isTransactionSaveError(exception.getCause())) {
            apiResponse.setCode(ErrorCode.TRANSACTION_SAVE_FAILED.getCode());
            apiResponse.setMessage(ErrorCode.TRANSACTION_SAVE_FAILED.getMessage());
        } else {
            apiResponse.setCode(ErrorCode.INVALID_REQUEST.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_REQUEST.getMessage());
        }
        return ResponseEntity.badRequest().body(apiResponse);
    }

    /** Transaction bị rollback → trả 400; nếu liên quan lưu transaction thì trả 1031 + hướng dẫn chạy SQL */
    @ExceptionHandler(value = UnexpectedRollbackException.class)
    ResponseEntity<ApiResponse> handleUnexpectedRollback(UnexpectedRollbackException exception) {
        log.warn("Transaction rolled back: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        if (isTransactionSaveError(exception) || isTransactionSaveError(exception.getCause())) {
            apiResponse.setCode(ErrorCode.TRANSACTION_SAVE_FAILED.getCode());
            apiResponse.setMessage(ErrorCode.TRANSACTION_SAVE_FAILED.getMessage());
        } else {
            apiResponse.setCode(ErrorCode.INVALID_REQUEST.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_REQUEST.getMessage());
        }
        return ResponseEntity.badRequest().body(apiResponse);
    }

    /** Thiếu hoặc không đủ quyền (PreAuthorize fail) → trả 403, không trả 500 */
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNAUTHORIZED.getMessage());
        return ResponseEntity.status(403).body(apiResponse);
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
