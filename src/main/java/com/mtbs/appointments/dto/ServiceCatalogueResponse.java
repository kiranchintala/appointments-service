package com.mtbs.appointments.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceCatalogueResponse {

    private UUID id;
    private String name;
    private String description;
    private double price;
    private int durationInMinutes;
    private boolean active;
}
