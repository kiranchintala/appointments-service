package com.mtbs.appointments.exception;

/**
 * Custom exception to indicate an optimistic locking conflict during an update operation.
 * This typically means another user or process modified the same resource concurrently.
 * Annotated with @ResponseStatus to return an HTTP 409 Conflict status.
 */
public class OptimisticLockingConflictException extends RuntimeException {

    public OptimisticLockingConflictException(String message) {
        super(message);
    }

    public OptimisticLockingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
