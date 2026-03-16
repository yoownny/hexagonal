package com.ahnlab.edr.sample.core.application.exception;

/**
 * 모든 도메인 에러 코드가 구현해야 하는 인터페이스.
 * 각 도메인별 enum으로 구현한다.
 */
public interface ErrorCode {

    /**
     * 에러 식별 코드 (예: EVT-101)
     *
     * @return 에러 코드 문자열
     */
    String getCode();

    /**
     * 에러 메시지 템플릿 (%s 포맷 지원)
     *
     * @return 메시지 템플릿
     */
    String getMessage();

    /**
     * HTTP 상태 코드
     *
     * @return HTTP 상태 코드
     */
    int getHttpStatus();
}
