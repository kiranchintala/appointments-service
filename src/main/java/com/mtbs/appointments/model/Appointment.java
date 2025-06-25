package com.mtbs.appointments.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userId;

    /**
     * Defines the OneToMany relationship with the Service entity.
     * 'mappedBy = "appointment"': Indicates that the 'appointment' field in the Service entity
     * is the owning side of this bidirectional relationship. Hibernate will use this field
     * to manage the foreign key relationship in the 'service' table.
     * 'cascade = CascadeType.ALL': All persistence operations (persist, merge, remove, refresh, detach)
     * performed on an Appointment entity will be cascaded to its associated Service entities.
     * 'orphanRemoval = true': If a Service is removed from the 'services' collection of an Appointment,
     * and it's no longer referenced by any other Appointment, it will be automatically deleted from the database.
     */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ServiceModel> services = new ArrayList<>();

    private LocalDateTime dateTime;
    private Integer guests;
    private String notes;
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double totalCost;

    @Version
    private Long version;

    public void addService(ServiceModel service) {
        services.add(service);
        service.setAppointment(this);
    }

    public void setServices(List<ServiceModel> services) {
        this.services.clear();
        if (services != null) {
            services.forEach(this::addService);
        }
    }


}

