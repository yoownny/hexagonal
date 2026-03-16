package com.ahnlab.edr.sample.core.application.exception;

import lombok.Getter;

/**
 * 모든 도메인 예외의 기반 클래스.
 * Outbound Adapter에서 직접 throw 금지 — Service에서만 사용.
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = errorCode.getMessage();
    }

    public ApplicationException(ErrorCode errorCode, String detail) {
        super(String.format(errorCode.getMessage(), detail));
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ApplicationException(ErrorCode errorCode, String detail, Throwable cause) {
        super(String.format(errorCode.getMessage(), detail), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
