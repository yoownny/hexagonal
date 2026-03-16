package com.ahnlab.edr.sample.bootstrap.common;

import com.ahnlab.edr.sample.core.application.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러.
 * ApplicationException → ApiResponse 변환 및 HTTP 상태 코드 매핑.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 예외 처리.
     *
     * @param e ApplicationException
     * @return 에러 응답
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException e) {
        log.warn("Application exception: code={}, detail={}", e.getErrorCode().getCode(), e.getDetail());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }

    /**
     * 처리되지 않은 예외 처리.
     *
     * @param e Exception
     * @return 500 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"));
    }
}
