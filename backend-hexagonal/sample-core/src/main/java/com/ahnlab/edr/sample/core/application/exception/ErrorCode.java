package com.ahnlab.edr.sample.core.application.exception;

/**
 * Common interface for all error codes in the application.
 * Each domain should define its own error code enum implementing this interface.
 */
public interface ErrorCode {
    
    /**
     * Gets the unique error code (e.g., "EVT-001", "USR-001").
     *
     * @return the error code
     */
    String getCode();
    
    /**
     * Gets the error message template.
     *
     * @return the error message
     */
    String getMessage();
    
    /**
     * Gets the HTTP status code associated with this error.
     *
     * @return the HTTP status code (e.g., 404, 500)
     */
    int getHttpStatus();
}
