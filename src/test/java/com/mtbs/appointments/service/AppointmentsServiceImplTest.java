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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for JUnit 5
class AppointmentsServiceImplTest {

    @Mock // Creates a mock instance of AppointmentsRepository
    private AppointmentsRepository appointmentsRepository;

    @Mock // Creates a mock instance of AppointmentMapper
    private AppointmentMapper appointmentMapper;

    @InjectMocks // Injects the mocks into AppointmentsServiceImpl
    private AppointmentsServiceImpl appointmentsService;

    // --- Common test data ---
    private UUID appointmentId;
    private String userId;
    private LocalDateTime dateTime;
    private ServiceDTO serviceDTO;
    private ServiceModel serviceModel;
    private Appointment appointment;
    private AppointmentResponse appointmentResponse;
    private CreateAppointmentRequest createRequest;
    private UpdateAppointmentRequest updateRequest;

    @BeforeEach // Sets up common data before each test method runs
    void setUp() {
        appointmentId = UUID.randomUUID();
        userId = "testUser123";
        dateTime = LocalDateTime.of(2025, 12, 25, 10, 0);

        // ServiceDTO for requests
        serviceDTO = new ServiceDTO("Facial", 70.0, "Relaxing facial treatment.");

        // ServiceModel (JPA Entity)
        serviceModel = new ServiceModel(1L, "Facial", 70.0, "Relaxing facial treatment.", null); // appointment field is null initially

        // Appointment (JPA Entity)
        appointment = new Appointment(
                appointmentId,
                userId,
                Arrays.asList(serviceModel), // List of ServiceModel entities
                dateTime,
                1,
                "No notes",
                "Pending",
                LocalDateTime.now(),
                LocalDateTime.now(),
                70.0,
                0L // Version for optimistic locking
        );
        serviceModel.setAppointment(appointment); // Set bidirectional link

        // AppointmentResponse (DTO for output)
        appointmentResponse = new AppointmentResponse(
                appointmentId,
                userId,
                Arrays.asList(serviceDTO), // List of ServiceDTOs
                dateTime,
                1,
                "No notes",
                "Pending",
                LocalDateTime.now(),
                LocalDateTime.now(),
                70.0
        );

        // CreateAppointmentRequest (DTO for input)
        createRequest = new CreateAppointmentRequest(
                userId,
                Arrays.asList(serviceDTO),
                dateTime,
                1,
                "No notes",
                70.00
        );

        // UpdateAppointmentRequest (DTO for input)
        updateRequest = new UpdateAppointmentRequest(
                userId,
                Arrays.asList(serviceDTO),
                dateTime.plusDays(1), // Updated date
                2, // Updated guests
                "Updated notes",
                "Confirmed" ,
                90.00
        );
    }

    // --- createAppointment tests ---
    @Test
    @DisplayName("Should create an appointment successfully")
    void createAppointment_Success() {
        // Mock mapper behavior: DTO to Entity
        when(appointmentMapper.toEntity(any(CreateAppointmentRequest.class))).thenReturn(appointment);
        // Mock repository behavior: save
        when(appointmentsRepository.save(any(Appointment.class))).thenReturn(appointment);
        // Mock mapper behavior: Entity to Response DTO
        when(appointmentMapper.toResponseDto(any(Appointment.class))).thenReturn(appointmentResponse);

        // Call the service method
        AppointmentResponse result = appointmentsService.createAppointment(createRequest);

        // Assertions
        assertNotNull(result);
        assertEquals(appointmentId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getServices().size());
        assertEquals(70.0, result.getTotalCost());
        assertEquals("Pending", result.getStatus());

        // Verify interactions with mocks
        verify(appointmentMapper).toEntity(createRequest);
        verify(appointmentsRepository).save(any(Appointment.class));
        verify(appointmentMapper).toResponseDto(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw AppointmentCreationException on DataAccessException during creation")
    void createAppointment_DataAccessException() {
        when(appointmentMapper.toEntity(any(CreateAppointmentRequest.class))).thenReturn(appointment);
        // Simulate a database error when saving
        when(appointmentsRepository.save(any(Appointment.class))).thenThrow(new DataAccessException("DB error") {});

        // Assert that the custom exception is thrown
        AppointmentCreationException exception = assertThrows(AppointmentCreationException.class, () ->
                appointmentsService.createAppointment(createRequest)
        );

        // Verify the exception message and cause
        assertTrue(exception.getMessage().contains("Failed to create appointment due to a database error."));
        assertTrue(exception.getCause() instanceof DataAccessException);

        verify(appointmentMapper).toEntity(createRequest);
        verify(appointmentsRepository).save(any(Appointment.class));
        verify(appointmentMapper, never()).toResponseDto(any(Appointment.class)); // Should not reach this
    }

    @Test
    @DisplayName("Should throw AppointmentCreationException on generic Exception during creation")
    void createAppointment_GenericException() {
        when(appointmentMapper.toEntity(any(CreateAppointmentRequest.class))).thenReturn(appointment);
        // Simulate a generic unexpected error
        when(appointmentsRepository.save(any(Appointment.class))).thenThrow(new RuntimeException("Unexpected error"));

        // Assert that the custom exception is thrown
        AppointmentCreationException exception = assertThrows(AppointmentCreationException.class, () ->
                appointmentsService.createAppointment(createRequest)
        );

        // Verify the exception message and cause
        assertTrue(exception.getMessage().contains("An unexpected error occurred during appointment creation."));
        assertTrue(exception.getCause() instanceof RuntimeException);

        verify(appointmentMapper).toEntity(createRequest);
        verify(appointmentsRepository).save(any(Appointment.class));
        verify(appointmentMapper, never()).toResponseDto(any(Appointment.class));
    }


    // --- getAllAppointments tests ---
    @Test
    @DisplayName("Should retrieve all appointments successfully")
    void getAllAppointments_Success() {
        List<Appointment> appointments = Arrays.asList(appointment);
        List<AppointmentResponse> expectedResponses = Arrays.asList(appointmentResponse);

        when(appointmentsRepository.findAll()).thenReturn(appointments);
        // Mock toDtoList as it's directly called
        when(appointmentMapper.toDtoList(appointments)).thenReturn(expectedResponses);

        List<AppointmentResponse> result = appointmentsService.getAllAppointments();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedResponses.get(0).getId(), result.get(0).getId());

        verify(appointmentsRepository).findAll();
        verify(appointmentMapper).toDtoList(appointments);
    }

    @Test
    @DisplayName("Should throw AppointmentRetrievalException on DataAccessException during getAllAppointments")
    void getAllAppointments_DataAccessException() {
        when(appointmentsRepository.findAll()).thenThrow(new DataAccessException("DB error") {});

        AppointmentRetrievalException exception = assertThrows(AppointmentRetrievalException.class, () ->
                appointmentsService.getAllAppointments()
        );

        assertTrue(exception.getMessage().contains("Failed to retrieve appointments due to a database error."));
        assertTrue(exception.getCause() instanceof DataAccessException);

        verify(appointmentsRepository).findAll();
        verify(appointmentMapper, never()).toDtoList(anyList());
    }

    @Test
    @DisplayName("Should throw AppointmentRetrievalException on generic Exception during getAllAppointments")
    void getAllAppointments_GenericException() {
        when(appointmentsRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        AppointmentRetrievalException exception = assertThrows(AppointmentRetrievalException.class, () ->
                appointmentsService.getAllAppointments()
        );

        assertTrue(exception.getMessage().contains("An unexpected error occurred during retrieval of all appointments."));
        assertTrue(exception.getCause() instanceof RuntimeException);

        verify(appointmentsRepository).findAll();
        verify(appointmentMapper, never()).toDtoList(anyList());
    }

    // --- getAppointmentById tests ---
    @Test
    @DisplayName("Should retrieve appointment by ID successfully")
    void getAppointmentById_Success() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponse);

        Optional<AppointmentResponse> result = appointmentsService.getAppointmentById(appointmentId);

        assertTrue(result.isPresent());
        assertEquals(appointmentId, result.get().getId());
        assertEquals(userId, result.get().getUserId());

        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper).toResponseDto(appointment);
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when appointment not found by ID")
    void getAppointmentById_NotFound() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.empty());

        AppointmentNotFoundException exception = assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsService.getAppointmentById(appointmentId)
        );

        assertTrue(exception.getMessage().contains("Appointment with ID " + appointmentId + " not found."));
        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper, never()).toResponseDto(any(Appointment.class));
    }


    // --- updateAppointment tests ---
    @Test
    @DisplayName("Should update an appointment successfully")
    void updateAppointment_Success() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentMapper).updateEntityFromDto(any(UpdateAppointmentRequest.class), any(Appointment.class));
        when(appointmentsRepository.save(any(Appointment.class))).thenReturn(appointment); // Assuming save returns the updated entity
        when(appointmentMapper.toResponseDto(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentsService.updateAppointment(appointmentId, updateRequest);

        assertNotNull(result);
        assertEquals(appointmentId, result.getId());
        // Verify fields were updated (mocked behavior, but confirms flow)
        assertEquals(updateRequest.getUserId(), result.getUserId());
        // Add more assertions based on your update logic if needed

        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper).updateEntityFromDto(updateRequest, appointment);
        verify(appointmentsRepository).save(appointment);
        verify(appointmentMapper).toResponseDto(appointment);
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when updating non-existent appointment")
    void updateAppointment_NotFound() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.empty());

        AppointmentNotFoundException exception = assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsService.updateAppointment(appointmentId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Appointment with ID " + appointmentId + " not found for update."));
        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper, never()).updateEntityFromDto(any(), any());
        verify(appointmentsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw OptimisticLockingConflictException on optimistic locking failure during update")
    void updateAppointment_OptimisticLockingFailure() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentMapper).updateEntityFromDto(any(), any());
        when(appointmentsRepository.save(any(Appointment.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException("Appointment", appointmentId.toString()));

        OptimisticLockingConflictException exception = assertThrows(OptimisticLockingConflictException.class, () ->
                appointmentsService.updateAppointment(appointmentId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Appointment updated by another user. Please retry your operation."));
        assertTrue(exception.getCause() instanceof ObjectOptimisticLockingFailureException);
        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper).updateEntityFromDto(updateRequest, appointment);
        verify(appointmentsRepository).save(appointment);
    }

    @Test
    @DisplayName("Should throw AppointmentUpdateException on DataAccessException during update")
    void updateAppointment_DataAccessException() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentMapper).updateEntityFromDto(any(), any());
        when(appointmentsRepository.save(any(Appointment.class))).thenThrow(new DataAccessException("DB error") {});

        AppointmentUpdateException exception = assertThrows(AppointmentUpdateException.class, () ->
                appointmentsService.updateAppointment(appointmentId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Failed to update appointment due to a database error."));
        assertTrue(exception.getCause() instanceof DataAccessException);
        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper).updateEntityFromDto(updateRequest, appointment);
        verify(appointmentsRepository).save(appointment);
    }

    @Test
    @DisplayName("Should throw AppointmentUpdateException on generic Exception during update")
    void updateAppointment_GenericException() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentMapper).updateEntityFromDto(any(), any());
        when(appointmentsRepository.save(any(Appointment.class))).thenThrow(new RuntimeException("Unexpected error"));

        AppointmentUpdateException exception = assertThrows(AppointmentUpdateException.class, () ->
                appointmentsService.updateAppointment(appointmentId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("An unexpected error occurred during appointment update."));
        assertTrue(exception.getCause() instanceof RuntimeException);
        verify(appointmentsRepository).findById(appointmentId);
        verify(appointmentMapper).updateEntityFromDto(updateRequest, appointment);
        verify(appointmentsRepository).save(appointment);
    }

    // --- deleteAppointment tests ---
    @Test
    @DisplayName("Should delete an appointment successfully")
    void deleteAppointment_Success() {
        when(appointmentsRepository.existsById(appointmentId)).thenReturn(true);
        doNothing().when(appointmentsRepository).deleteById(appointmentId);

        boolean result = appointmentsService.deleteAppointment(appointmentId);

        assertTrue(result);
        verify(appointmentsRepository).existsById(appointmentId);
        verify(appointmentsRepository).deleteById(appointmentId);
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when deleting non-existent appointment")
    void deleteAppointment_NotFound() {
        when(appointmentsRepository.existsById(appointmentId)).thenReturn(false);

        AppointmentNotFoundException exception = assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsService.deleteAppointment(appointmentId)
        );

        assertTrue(exception.getMessage().contains("Appointment with ID " + appointmentId + " not found for deletion."));
        verify(appointmentsRepository).existsById(appointmentId);
        verify(appointmentsRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException on DataAccessException during deletion")
    void deleteAppointment_DataAccessException() {
        when(appointmentsRepository.existsById(appointmentId)).thenReturn(true);
        doThrow(new EmptyResultDataAccessException("DB error", 1)).when(appointmentsRepository).deleteById(appointmentId); // Simulate DB access error

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentsService.deleteAppointment(appointmentId)
        );

        assertTrue(exception.getMessage().contains("Failed to delete appointment due to a database error."));
        assertTrue(exception.getCause() instanceof EmptyResultDataAccessException);
        verify(appointmentsRepository).existsById(appointmentId);
        verify(appointmentsRepository).deleteById(appointmentId);
    }

    @Test
    @DisplayName("Should throw RuntimeException on generic Exception during deletion")
    void deleteAppointment_GenericException() {
        when(appointmentsRepository.existsById(appointmentId)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(appointmentsRepository).deleteById(appointmentId);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentsService.deleteAppointment(appointmentId)
        );

        assertTrue(exception.getMessage().contains("An unexpected error occurred during booking deletion."));
        assertTrue(exception.getCause() instanceof RuntimeException);
        verify(appointmentsRepository).existsById(appointmentId);
        verify(appointmentsRepository).deleteById(appointmentId);
    }
}
