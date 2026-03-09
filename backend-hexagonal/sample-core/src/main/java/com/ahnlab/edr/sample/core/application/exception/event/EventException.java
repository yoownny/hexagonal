package com.ahnlab.edr.sample.core.application.exception.event;

import com.ahnlab.edr.sample.core.application.exception.ApplicationException;

/**
 * Exception for Event domain operations.
 * All event-related errors should use this exception with appropriate {@link EventErrorCode}.
 * 
 * <p>Example usage:</p>
 * <pre>
 * throw new EventException(EventErrorCode.EVENT_NOT_FOUND, eventId);
 * throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, cause, eventId);
 * </pre>
 */
public class EventException extends ApplicationException {
    
    /**
     * Constructs a new EventException with an error code.
     *
     * @param errorCode the event error code
     */
    public EventException(EventErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * Constructs a new EventException with an error code and message arguments.
     *
     * @param errorCode the event error code
     * @param messageArgs arguments to format the error message (e.g., event ID)
     */
    public EventException(EventErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
    
    /**
     * Constructs a new EventException with an error code and cause.
     *
     * @param errorCode the event error code
     * @param cause the cause of this exception
     */
    public EventException(EventErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * Constructs a new EventException with an error code, cause, and message arguments.
     *
     * @param errorCode the event error code
     * @param cause the cause of this exception
     * @param messageArgs arguments to format the error message
     */
    public EventException(EventErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode, cause, messageArgs);
    }
}
