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
public class UpdateAppointmentRequest {

    @NotEmpty(message = "At least one service ID must be provided")
    private List<UUID> serviceIds;

    @FutureOrPresent(message = "Appointment date and time must be in the future or present")
    private LocalDateTime dateTime;

    @PositiveOrZero(message = "Number of guests cannot be negative")
    private Integer guests;

    private String notes;

    @NotBlank
    private String status;

}
