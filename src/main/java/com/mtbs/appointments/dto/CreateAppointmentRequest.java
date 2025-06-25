package com.mtbs.appointments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    @NotEmpty(message = "At least one service ID must be provided")
    private List<UUID> serviceIds;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date and time must be in the future")
    private LocalDateTime dateTime;

    @PositiveOrZero(message = "Number of guests cannot be negative")
    private int guests;

    private String notes;

    @NotBlank
    private String status;
}
