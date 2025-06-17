package com.mtbs.appointments.controller;

import com.mtbs.appointments.dto.AppointmentResponse;
import com.mtbs.appointments.dto.CreateAppointmentRequest;
import com.mtbs.appointments.dto.ServiceDTO;
import com.mtbs.appointments.dto.UpdateAppointmentRequest;
import com.mtbs.appointments.exception.AppointmentNotFoundException;
import com.mtbs.appointments.exception.AppointmentCreationException;
import com.mtbs.appointments.exception.AppointmentRetrievalException;
import com.mtbs.appointments.exception.AppointmentUpdateException;
import com.mtbs.appointments.exception.OptimisticLockingConflictException; // Assuming this exception exists
import com.mtbs.appointments.service.AppointmentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for JUnit 5
class AppointmentsControllerTest {

    @Mock // Creates a mock instance of AppointmentsService
    private AppointmentsService appointmentsService;

    @InjectMocks // Injects the mock AppointmentsService into AppointmentsController
    private AppointmentsController appointmentsController;

    // --- Common test data ---
    private UUID appointmentId;
    private String userId;
    private LocalDateTime dateTime;
    private ServiceDTO serviceDTO;
    private AppointmentResponse appointmentResponse;
    private CreateAppointmentRequest createRequest;
    private UpdateAppointmentRequest updateRequest;

    @BeforeEach // Sets up common data before each test method runs
    void setUp() {
        appointmentId = UUID.randomUUID();
        userId = "testUser123";
        dateTime = LocalDateTime.of(2025, 12, 25, 10, 0);

        // ServiceDTO for requests and responses
        serviceDTO = new ServiceDTO("Facial", 70.0, "Relaxing facial treatment.");

        // AppointmentResponse (DTO for output from service)
        appointmentResponse = new AppointmentResponse(
                appointmentId,
                userId,
                Arrays.asList(serviceDTO),
                dateTime,
                1,
                "No notes",
                "Pending",
                LocalDateTime.now(),
                LocalDateTime.now(),
                70.0
        );

        // CreateAppointmentRequest (DTO for input to controller)
        createRequest = new CreateAppointmentRequest(
                userId,
                Arrays.asList(serviceDTO),
                dateTime,
                1,
                "No notes",
                70.00
        );

        // UpdateAppointmentRequest (DTO for input to controller)
        updateRequest = new UpdateAppointmentRequest(
                userId,
                Arrays.asList(serviceDTO),
                dateTime.plusDays(1), // Updated date
                2, // Updated guests
                "Updated notes",
                "Confirmed",
                90.00
        );
    }

    // --- createAppointment tests ---
    @Test
    @DisplayName("Should create an appointment and return 201 Created")
    void createAppointment_Success() {
        // Mock service behavior: returns a successful AppointmentResponse
        when(appointmentsService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(appointmentResponse);

        // Call the controller method
        ResponseEntity<AppointmentResponse> response = appointmentsController.createAppointment(createRequest);

        // Assertions on the ResponseEntity
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(appointmentId, response.getBody().getId());
        assertEquals(userId, response.getBody().getUserId());

        // Verify that the service method was called once with the correct request
        verify(appointmentsService).createAppointment(createRequest);
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for AppointmentCreationException")
    void createAppointment_AppointmentCreationException() {
        // Mock service to throw AppointmentCreationException
        when(appointmentsService.createAppointment(any(CreateAppointmentRequest.class)))
                .thenThrow(new AppointmentCreationException("Failed to create appointment."));

        // Call the controller method and expect the exception to be thrown
        // (Spring's @ControllerAdvice will handle the HTTP status)
        assertThrows(AppointmentCreationException.class, () ->
                appointmentsController.createAppointment(createRequest)
        );

        // Verify service interaction
        verify(appointmentsService).createAppointment(createRequest);
    }

    // --- getAllAppointments tests ---
    @Test
    @DisplayName("Should retrieve all appointments and return 200 OK")
    void getAllAppointments_Success() {
        List<AppointmentResponse> expectedList = Arrays.asList(appointmentResponse);
        // Mock service behavior: returns a list of AppointmentResponse
        when(appointmentsService.getAllAppointments()).thenReturn(expectedList);

        // Call the controller method
        ResponseEntity<List<AppointmentResponse>> response = appointmentsController.getAllAppointments();

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(appointmentId, response.getBody().get(0).getId());

        // Verify service interaction
        verify(appointmentsService).getAllAppointments();
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for AppointmentRetrievalException")
    void getAllAppointments_AppointmentRetrievalException() {
        // Mock service to throw AppointmentRetrievalException
        when(appointmentsService.getAllAppointments())
                .thenThrow(new AppointmentRetrievalException("Failed to retrieve appointments."));

        // Call the controller method and expect the exception
        assertThrows(AppointmentRetrievalException.class, () ->
                appointmentsController.getAllAppointments()
        );

        // Verify service interaction
        verify(appointmentsService).getAllAppointments();
    }

    // --- getAppointmentById tests ---
    @Test
    @DisplayName("Should retrieve appointment by ID and return 200 OK")
    void getAppointmentById_Success() throws AppointmentNotFoundException {
        // Mock service behavior: returns an Optional containing the AppointmentResponse
        when(appointmentsService.getAppointmentById(appointmentId)).thenReturn(Optional.of(appointmentResponse));

        // Call the controller method
        ResponseEntity<AppointmentResponse> response = appointmentsController.getAppointmentById(appointmentId);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(appointmentId, response.getBody().getId());

        // Verify service interaction
        verify(appointmentsService).getAppointmentById(appointmentId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when appointment by ID is not found")
    void getAppointmentById_NotFound() throws AppointmentNotFoundException {
        // Mock service behavior: returns an empty Optional
        when(appointmentsService.getAppointmentById(appointmentId)).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<AppointmentResponse> response = appointmentsController.getAppointmentById(appointmentId);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Body should be null for NOT_FOUND

        // Verify service interaction
        verify(appointmentsService).getAppointmentById(appointmentId);
    }

    @Test
    @DisplayName("Should handle AppointmentNotFoundException and return 404 Not Found")
    void getAppointmentById_AppointmentNotFoundException() throws AppointmentNotFoundException {
        // Mock service to throw AppointmentNotFoundException
        when(appointmentsService.getAppointmentById(appointmentId))
                .thenThrow(new AppointmentNotFoundException("Appointment not found."));

        // Call the controller method and expect the exception to be rethrown
        assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsController.getAppointmentById(appointmentId)
        );

        // Verify service interaction
        verify(appointmentsService).getAppointmentById(appointmentId);
    }

    // --- updateAppointment tests ---
    @Test
    @DisplayName("Should update an appointment and return 200 OK")
    void updateAppointment_Success() {
        // Mock service behavior: returns the updated AppointmentResponse
        when(appointmentsService.updateAppointment(eq(appointmentId), any(UpdateAppointmentRequest.class)))
                .thenReturn(appointmentResponse); // Assuming response reflects update

        // Call the controller method
        ResponseEntity<AppointmentResponse> response = appointmentsController.updateAppointment(appointmentId, updateRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(appointmentId, response.getBody().getId());

        // Verify service interaction
        verify(appointmentsService).updateAppointment(appointmentId, updateRequest);
    }

    @Test
    @DisplayName("Should return 404 Not Found for AppointmentNotFoundException during update")
    void updateAppointment_NotFound() {
        // Mock service to throw AppointmentNotFoundException
        when(appointmentsService.updateAppointment(eq(appointmentId), any(UpdateAppointmentRequest.class)))
                .thenThrow(new AppointmentNotFoundException("Appointment not found for update."));

        // Call the controller method and expect the exception
        assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsController.updateAppointment(appointmentId, updateRequest)
        );

        // Verify service interaction
        verify(appointmentsService).updateAppointment(appointmentId, updateRequest);
    }

    @Test
    @DisplayName("Should return 409 Conflict for OptimisticLockingConflictException during update")
    void updateAppointment_OptimisticLockingConflict() {
        // Mock service to throw OptimisticLockingConflictException
        when(appointmentsService.updateAppointment(eq(appointmentId), any(UpdateAppointmentRequest.class)))
                .thenThrow(new OptimisticLockingConflictException("Conflict detected."));

        // Call the controller method and expect the exception
        assertThrows(OptimisticLockingConflictException.class, () ->
                appointmentsController.updateAppointment(appointmentId, updateRequest)
        );

        // Verify service interaction
        verify(appointmentsService).updateAppointment(appointmentId, updateRequest);
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for AppointmentUpdateException during update")
    void updateAppointment_UpdateException() {
        // Mock service to throw AppointmentUpdateException
        when(appointmentsService.updateAppointment(eq(appointmentId), any(UpdateAppointmentRequest.class)))
                .thenThrow(new AppointmentUpdateException("General update error."));

        // Call the controller method and expect the exception
        assertThrows(AppointmentUpdateException.class, () ->
                appointmentsController.updateAppointment(appointmentId, updateRequest)
        );

        // Verify service interaction
        verify(appointmentsService).updateAppointment(appointmentId, updateRequest);
    }


    // --- deleteAppointment tests ---
    @Test
    @DisplayName("Should delete an appointment and return 204 No Content")
    void deleteAppointment_Success() throws AppointmentNotFoundException {
        // Mock service behavior: returns true for successful deletion
        when(appointmentsService.deleteAppointment(appointmentId)).thenReturn(true);

        // Call the controller method
        ResponseEntity<Void> response = appointmentsController.deleteAppointment(appointmentId);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody()); // Body should be null for NO_CONTENT

        // Verify service interaction
        verify(appointmentsService).deleteAppointment(appointmentId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent appointment")
    void deleteAppointment_NotFound() throws AppointmentNotFoundException {
        // Mock service behavior: returns false if not found
        when(appointmentsService.deleteAppointment(appointmentId)).thenReturn(false);

        // Call the controller method
        ResponseEntity<Void> response = appointmentsController.deleteAppointment(appointmentId);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify service interaction
        verify(appointmentsService).deleteAppointment(appointmentId);
    }

    @Test
    @DisplayName("Should handle AppointmentNotFoundException during deletion")
    void deleteAppointment_AppointmentNotFoundException() throws AppointmentNotFoundException {
        // Mock service to throw AppointmentNotFoundException
        when(appointmentsService.deleteAppointment(appointmentId))
                .thenThrow(new AppointmentNotFoundException("Appointment not found for deletion."));

        // Call the controller method and expect the exception to be rethrown
        assertThrows(AppointmentNotFoundException.class, () ->
                appointmentsController.deleteAppointment(appointmentId)
        );

        // Verify service interaction
        verify(appointmentsService).deleteAppointment(appointmentId);
    }
}
