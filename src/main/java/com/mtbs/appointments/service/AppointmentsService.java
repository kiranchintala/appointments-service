package com.mtbs.appointments.service;

import com.mtbs.appointments.dto.AppointmentResponse;
import com.mtbs.appointments.dto.CreateAppointmentRequest;
import com.mtbs.appointments.dto.UpdateAppointmentRequest;
import com.mtbs.appointments.model.Appointment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentsService {

    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    List<AppointmentResponse> getAllAppointments();

    Optional<AppointmentResponse> getAppointmentById(UUID id);

    AppointmentResponse updateAppointment(UUID id, UpdateAppointmentRequest updatedRequest);

    void deleteAppointment(UUID id);
}
