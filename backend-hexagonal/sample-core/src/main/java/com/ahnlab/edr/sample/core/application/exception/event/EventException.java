package com.ahnlab.edr.sample.core.application.exception.event;

import com.ahnlab.edr.sample.core.application.exception.ApplicationException;

/**
 * 이벤트 도메인 예외.
 * Service에서만 throw하며, EventErrorCode와 1:1로 매핑된다.
 */
public class EventException extends ApplicationException {

    public EventException(EventErrorCode errorCode) {
        super(errorCode);
    }

    public EventException(EventErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public EventException(EventErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
