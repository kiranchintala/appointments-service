package com.mtbs.appointments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private String userId;

    @Size(min = 1, message = "At least one service is required if updating services")
    @Valid
    private List<ServiceDTO> services;

    @FutureOrPresent(message = "Appointment date and time must be in the future or present")
    private LocalDateTime dateTime;

    @PositiveOrZero(message = "Number of guests cannot be negative")
    private Integer guests;

    private String notes;

    private String status;

    @PositiveOrZero(message = "Total cost must be a non-negative value")
    private Double totalCost;
}
