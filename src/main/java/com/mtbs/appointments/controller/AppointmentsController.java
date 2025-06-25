package com.mtbs.appointments.controller;

import com.mtbs.appointments.dto.AppointmentResponse;
import com.mtbs.appointments.dto.CreateAppointmentRequest;
import com.mtbs.appointments.dto.UpdateAppointmentRequest;
import com.mtbs.appointments.exception.AppointmentNotFoundException;
import com.mtbs.appointments.service.AppointmentsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    @Autowired
    public AppointmentsController(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse createdAppointment = appointmentsService.createAppointment(request);
        return new ResponseEntity<>(createdAppointment, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentsService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) throws AppointmentNotFoundException {
        Optional<AppointmentResponse> currentAppointment = appointmentsService.getAppointmentById(id);
        return currentAppointment.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable UUID id, @Valid @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse updatedBooking = appointmentsService.updateAppointment(id, request);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) throws AppointmentNotFoundException {
        appointmentsService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
