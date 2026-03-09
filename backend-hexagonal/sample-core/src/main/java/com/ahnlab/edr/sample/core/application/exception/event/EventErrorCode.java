package com.ahnlab.edr.sample.core.application.exception.event;

import com.ahnlab.edr.sample.core.application.exception.ErrorCode;

/**
 * Error codes for Event domain operations.
 * Each code follows the pattern: EVT-XXX
 */
public enum EventErrorCode implements ErrorCode {
    
    // Query errors (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event with id '%s'", 500),
    
    // Command errors (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event with id '%s'", 500),
    EVENT_DELETE_FAILED("EVT-202", "Failed to delete event with id '%s'", 500),
    
    // Validation errors (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400),
    EVENT_INVALID_DATA("EVT-303", "Invalid event data: %s", 400),
    EVENT_ALREADY_EXISTS("EVT-304", "Event with id '%s' already exists", 409);
    
    private final String code;
    private final String message;
    private final int httpStatus;
    
    EventErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}
