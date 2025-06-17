package com.mtbs.appointments.service;

import com.mtbs.appointments.dto.AppointmentResponse;
import com.mtbs.appointments.dto.CreateAppointmentRequest;
import com.mtbs.appointments.dto.ServiceDTO;
import com.mtbs.appointments.dto.UpdateAppointmentRequest;
import com.mtbs.appointments.exception.*;
import com.mtbs.appointments.mapper.AppointmentMapper;
import com.mtbs.appointments.model.Appointment;
import com.mtbs.appointments.model.ServiceModel;
import com.mtbs.appointments.repository.AppointmentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentsServiceImpl implements AppointmentsService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentsServiceImpl.class);
    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentMapper appointmentMapper;

    @Autowired
    public AppointmentsServiceImpl(AppointmentsRepository appointmentsRepository, AppointmentMapper appointmentMapper) {
        this.appointmentsRepository = appointmentsRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) throws AppointmentCreationException {
        try {
        // Map the DTO to the Appointment entity
        Appointment appointment = appointmentMapper.toEntity(request);

        // Set appointment-specific fields that are not part of the request DTO
        appointment.setId(UUID.randomUUID()); // Generate a unique ID for the new appointment
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.setStatus("Pending"); // Set an initial status for the new appointment

        // Calculate total cost and establish the bidirectional relationship for each service
        double totalCost = 0.0;
        if (appointment.getServices() != null) {
            for (ServiceModel service : appointment.getServices()) {
                // IMPORTANT: Set the 'appointment' reference on the 'Service' entity
                // This is crucial for Hibernate to correctly manage the @ManyToOne side
                service.setAppointment(appointment);
                totalCost += service.getPrice();
            }
        }
        appointment.setTotalCost(totalCost); // Set the calculated total cost

        // Save the new appointment (and its services due to CascadeType.ALL)
        Appointment savedAppointment = appointmentsRepository.save(appointment);

        // Map the saved entity back to a response DTO
        return appointmentMapper.toResponseDto(savedAppointment);
        }
        catch (DataAccessException e) {
            logger.error("Database error during appointment creation for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new AppointmentCreationException("Failed to create appointment due to a database error.", e);
        }
        catch (Exception e) {
            logger.error("An unexpected error during appointment creation for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new AppointmentCreationException("An unexpected error during appointment creation", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() throws AppointmentRetrievalException {
        try {
            return appointmentMapper.toDtoList(appointmentsRepository.findAll());
        } catch (DataAccessException e) {
            logger.error("Database error during retrieval of all appointments: {}", e.getMessage(), e);
            throw new AppointmentRetrievalException("Failed to retrieve appointments due to a database error.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during retrieval of all appointments: {}", e.getMessage(), e);
            throw new AppointmentRetrievalException("An unexpected error occurred during retrieval of all appointments.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentResponse> getAppointmentById(UUID id) {
        Optional<Appointment> appointmentOptional = appointmentsRepository.findById(id);
        if (appointmentOptional.isEmpty()){
            throw new AppointmentNotFoundException("Appointment with ID " + id + " not found.");
        }
        return appointmentOptional.map(appointmentMapper::toResponseDto);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(UUID id, UpdateAppointmentRequest request) throws AppointmentNotFoundException, OptimisticLockingConflictException, AppointmentUpdateException {
        // Find the existing appointment, or throw an exception if not found
        // Use the correct repository name: appointmentRepository
        Appointment existingAppointment = appointmentsRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment with ID " + id + " not found for update."));

        // Use MapStruct to update the existing entity from the DTO
        appointmentMapper.updateEntityFromDto(request, existingAppointment);

        // Manually handle the services collection to ensure correct relationship management
        // First, clear all existing services. Due to 'orphanRemoval = true', these will be deleted from DB.
        existingAppointment.getServices().clear();
        double totalCost = 0.0;

        // Then, add the new services from the request
        if (request.getServices() != null) {
            for (ServiceDTO serviceDTO : request.getServices()) {
                ServiceModel newServiceModel = appointmentMapper.toServiceEntity(serviceDTO);
                existingAppointment.addService(newServiceModel); // Use helper to set bidirectional link
                totalCost += newServiceModel.getPrice();
            }
        }
        existingAppointment.setTotalCost(totalCost);
        existingAppointment.setUpdatedAt(LocalDateTime.now()); // Update the timestamp

        try {
            // Use the correct repository name: appointmentRepository
            Appointment updatedAppointment = appointmentsRepository.save(existingAppointment);
            return appointmentMapper.toResponseDto(updatedAppointment);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking conflict detected for appointment ID {}: {}", id, e.getMessage(), e);
            throw new OptimisticLockingConflictException("Appointment updated by another user. Please retry your operation.", e);
        } catch (DataAccessException e) {
            logger.error("Database error during appointment update for ID {}: {}", id, e.getMessage(), e);
            throw new AppointmentUpdateException("Failed to update appointment due to a database error.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during appointment update for ID {}: {}", id, e.getMessage(), e);
            throw new AppointmentUpdateException("An unexpected error occurred during appointment update.", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteAppointment(UUID id) throws AppointmentNotFoundException {
        if (!appointmentsRepository.existsById(id)) {
            throw new AppointmentNotFoundException("Appointment with ID " + id + " not found for deletion.");
        }
        try {
            appointmentsRepository.deleteById(id);
            return true;
        } catch (DataAccessException e) {
            logger.error("Database error during appointment deletion for ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete appointment due to a database error.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during appointment deletion for ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during booking deletion.", e);
        }
    }
}
