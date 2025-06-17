package com.mtbs.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {

    @NotBlank(message = "Service name cannot be empty")
    private String name;

    @NotNull(message = "Service price cannot be null")
    @PositiveOrZero(message = "Service price must be a non-negative value")
    private Double price;

    @NotBlank(message = "Service description cannot be empty")
    private String description;
}
