package com.mtbs.appointments.exception;

public class AppointmentRetrievalException extends RuntimeException {

    public AppointmentRetrievalException(String message) {
        super(message);
    }

    public AppointmentRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
