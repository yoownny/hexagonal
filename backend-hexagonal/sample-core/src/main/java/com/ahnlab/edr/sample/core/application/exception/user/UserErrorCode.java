package com.ahnlab.edr.sample.core.application.exception.user;

import com.ahnlab.edr.sample.core.application.exception.ErrorCode;

/**
 * Error codes for User domain operations.
 * Each code follows the pattern: USR-XXX
 */
public enum UserErrorCode implements ErrorCode {
    
    // Query errors (USR-1XX)
    USER_NOT_FOUND("USR-101", "User with id '%s' not found", 404),
    USER_QUERY_FAILED("USR-102", "Failed to query user with id '%s'", 500),
    
    // Command errors (USR-2XX)
    USER_SAVE_FAILED("USR-201", "Failed to save user with id '%s'", 500),
    USER_DELETE_FAILED("USR-202", "Failed to delete user with id '%s'", 500),
    
    // Validation errors (USR-3XX)
    USER_ID_REQUIRED("USR-301", "User ID is required", 400),
    USER_NAME_REQUIRED("USR-302", "User name is required", 400),
    USER_INVALID_DATA("USR-303", "Invalid user data: %s", 400),
    USER_ALREADY_EXISTS("USR-304", "User with id '%s' already exists", 409);
    
    private final String code;
    private final String message;
    private final int httpStatus;
    
    UserErrorCode(String code, String message, int httpStatus) {
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
