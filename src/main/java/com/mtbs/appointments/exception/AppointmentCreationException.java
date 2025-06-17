package com.mtbs.appointments.exception;

public class AppointmentCreationException extends RuntimeException {

    public AppointmentCreationException(String message) {
        super(message);
    }

    public AppointmentCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
