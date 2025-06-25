package com.mtbs.appointments.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Table(name = "appointment_services")
@NoArgsConstructor
@AllArgsConstructor
public class ServiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID serviceCatalogueId;

    private String name;
    private double price;
    private String description;
    private int durationInMinutes;

    /**
     * Defines the ManyToOne relationship with the Appointment entity.
     * This 'appointment_id' column will be the foreign key in the 'service' table,
     * linking each service back to its parent appointment.
     * It's the owning side of the bidirectional relationship.
     */
    // Specifies the foreign key column name in the 'service' table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    @JsonBackReference
    private Appointment appointment;
}