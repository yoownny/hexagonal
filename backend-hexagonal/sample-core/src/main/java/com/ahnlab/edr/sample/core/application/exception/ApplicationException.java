package com.ahnlab.edr.sample.core.application.exception;

/**
 * Base exception for all application layer exceptions.
 * Every custom exception should extend this class and provide an error code.
 */
public class ApplicationException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] messageArgs;
    
    /**
     * Constructs a new application exception with an error code.
     *
     * @param errorCode the error code defining the type of error
     */
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = null;
    }
    
    /**
     * Constructs a new application exception with an error code and message arguments.
     *
     * @param errorCode the error code defining the type of error
     * @param messageArgs arguments to format the error message (e.g., ID, name)
     */
    public ApplicationException(ErrorCode errorCode, Object... messageArgs) {
        super(String.format(errorCode.getMessage(), messageArgs));
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }
    
    /**
     * Constructs a new application exception with an error code and cause.
     *
     * @param errorCode the error code defining the type of error
     * @param cause the cause of this exception
     */
    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }
    
    /**
     * Constructs a new application exception with an error code, message arguments, and cause.
     *
     * @param errorCode the error code defining the type of error
     * @param cause the cause of this exception
     * @param messageArgs arguments to format the error message
     */
    public ApplicationException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(String.format(errorCode.getMessage(), messageArgs), cause);
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }
    
    /**
     * Gets the error code associated with this exception.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error code string (e.g., "EVT-001").
     *
     * @return the error code string
     */
    public String getCode() {
        return errorCode.getCode();
    }
    
    /**
     * Gets the HTTP status code associated with this error.
     *
     * @return the HTTP status code
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
    
    /**
     * Gets the message arguments used to format the error message.
     *
     * @return the message arguments, or null if none
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }
}
