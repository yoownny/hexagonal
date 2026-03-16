package com.ahnlab.edr.sample.core.application.exception.event;

import com.ahnlab.edr.sample.core.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이벤트 도메인 에러 코드.
 * 번호 체계: EVT-1XX(Query), EVT-2XX(Command), EVT-3XX(Validation)
 */
@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {

    // Query (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event with id '%s'", 500),

    // Command (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event with id '%s'", 500),

    // Validation (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400);

    private final String code;
    private final String message;
    private final int httpStatus;
}
