package com.mtbs.appointments.exception;

public class AppointmentUpdateException extends RuntimeException {

    public AppointmentUpdateException(String message) {
        super(message);
    }

    public AppointmentUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}