package com.mtbs.appointments.repository;

import com.mtbs.appointments.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.services WHERE a.id = :id")
    Optional<Appointment> findByIdWithServices(UUID id);

    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.services")
    List<Appointment> findAllWithServices();
}
