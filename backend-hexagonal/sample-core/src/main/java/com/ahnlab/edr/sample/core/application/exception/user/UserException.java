package com.ahnlab.edr.sample.core.application.exception.user;

import com.ahnlab.edr.sample.core.application.exception.ApplicationException;

/**
 * Exception for User domain operations.
 * All user-related errors should use this exception with appropriate {@link UserErrorCode}.
 * 
 * <p>Example usage:</p>
 * <pre>
 * throw new UserException(UserErrorCode.USER_NOT_FOUND, userId);
 * throw new UserException(UserErrorCode.USER_SAVE_FAILED, cause, userId);
 * </pre>
 */
public class UserException extends ApplicationException {
    
    /**
     * Constructs a new UserException with an error code.
     *
     * @param errorCode the user error code
     */
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * Constructs a new UserException with an error code and message arguments.
     *
     * @param errorCode the user error code
     * @param messageArgs arguments to format the error message (e.g., user ID)
     */
    public UserException(UserErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
    
    /**
     * Constructs a new UserException with an error code and cause.
     *
     * @param errorCode the user error code
     * @param cause the cause of this exception
     */
    public UserException(UserErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * Constructs a new UserException with an error code, cause, and message arguments.
     *
     * @param errorCode the user error code
     * @param cause the cause of this exception
     * @param messageArgs arguments to format the error message
     */
    public UserException(UserErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode, cause, messageArgs);
    }
}
