package com.mtbs.appointments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private String userId;
    private List<ServiceDTO> services; // List of services
    private LocalDateTime dateTime; // Single date/time
    private Integer guests;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double totalCost;
}
