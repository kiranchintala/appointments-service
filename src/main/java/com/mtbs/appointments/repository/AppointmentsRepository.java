package com.mtbs.appointments.repository;

import com.mtbs.appointments.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointment, UUID> {
}
