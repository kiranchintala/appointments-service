package com.mtbs.appointments.service;

import com.mtbs.appointments.dto.*;
import com.mtbs.appointments.exception.*;
import com.mtbs.appointments.mapper.AppointmentMapper;
import com.mtbs.appointments.model.Appointment;
import com.mtbs.appointments.model.ServiceModel;
import com.mtbs.appointments.repository.AppointmentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AppointmentsServiceImpl implements AppointmentsService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentsServiceImpl.class);
    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentMapper appointmentMapper;
    private final WebClient catalogueServiceWebClient;

    @Autowired
    public AppointmentsServiceImpl(AppointmentsRepository appointmentsRepository, AppointmentMapper appointmentMapper, WebClient catalogueServiceWebClient) {
        this.appointmentsRepository = appointmentsRepository;
        this.appointmentMapper = appointmentMapper;
        this.catalogueServiceWebClient = catalogueServiceWebClient;
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        try {
            // Step 1: Fetch and validate services from the catalogue
            List<ServiceCatalogueResponse> fetchedServices = fetchAndVerifyServices(request.getServiceIds());

            // Step 2: Build the complete Appointment object graph in memory
            Appointment appointment = new Appointment();
            appointment.setUserId(request.getUserId());
            appointment.setDateTime(request.getDateTime());
            appointment.setNotes(request.getNotes());
            appointment.setGuests(request.getGuests());
            appointment.setCreatedAt(LocalDateTime.now());
            appointment.setUpdatedAt(LocalDateTime.now());
            appointment.setStatus("Confirmed");

            List<ServiceModel> serviceModels = fetchedServices.stream()
                    .peek(fs -> {
                        if (!fs.isActive()) throw new AppointmentCreationException("Service '" + fs.getName() + "' is currently inactive.");
                    })
                    .map(appointmentMapper::toServiceModel)
                    .peek(sm -> sm.setAppointment(appointment)) // Set the back-reference
                    .toList();

            serviceModels.forEach(appointment::addService); // Set the children on the parent

            double totalCost = serviceModels.stream().mapToDouble(ServiceModel::getPrice).sum();
            appointment.setTotalCost(totalCost);

            // Step 3: Persist the entire object graph in a single operation
            Appointment savedAppointment = appointmentsRepository.save(appointment);
            logger.info("Successfully created appointment {} for user {}", savedAppointment.getId(), request.getUserId());

            return appointmentMapper.toResponseDto(savedAppointment);

        } catch (Exception e) {
            logger.error("Failed to create appointment for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new AppointmentCreationException("An unexpected error occurred during appointment creation.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        logger.info("Fetching all appointments with their services");
        return appointmentMapper.toDtoList(appointmentsRepository.findAllWithServices());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentResponse> getAppointmentById(UUID id) {
        logger.info("Fetching appointment by ID with services: {}", id);
        return appointmentsRepository.findByIdWithServices(id).map(appointmentMapper::toResponseDto);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(UUID id, UpdateAppointmentRequest request) {
        try {
            Appointment existingAppointment = appointmentsRepository.findById(id)
                    .orElseThrow(() -> new AppointmentNotFoundException("Appointment with ID " + id + " not found."));

            List<ServiceCatalogueResponse> fetchedServices = fetchAndVerifyServices(request.getServiceIds());

            existingAppointment.getServices().clear();
            List<ServiceModel> serviceModels = fetchedServices.stream()
                    .peek(fs -> {
                        if (!fs.isActive()) throw new AppointmentCreationException("Service '" + fs.getName() + "' is currently inactive.");
                    })
                    .map(appointmentMapper::toServiceModel)
                    .peek(sm -> sm.setAppointment(existingAppointment))
                    .toList();

            existingAppointment.getServices().addAll(serviceModels);

            existingAppointment.setDateTime(request.getDateTime());
            existingAppointment.setNotes(request.getNotes());
            existingAppointment.setStatus(request.getStatus());
            existingAppointment.setUpdatedAt(LocalDateTime.now());
            existingAppointment.setTotalCost(serviceModels.stream().mapToDouble(ServiceModel::getPrice).sum());

            Appointment updatedAppointment = appointmentsRepository.save(existingAppointment);
            logger.info("Successfully updated appointment {}", updatedAppointment.getId());
            return appointmentMapper.toResponseDto(updatedAppointment);
        } catch (Exception e) {
            logger.error("Failed to update appointment {}: {}", id, e.getMessage(), e);
            throw new AppointmentUpdateException("An unexpected error occurred during appointment update.", e);
        }
    }

    @Override
    @Transactional
    public void deleteAppointment(UUID id) {
        logger.info("Deleting appointment with ID: {}", id);
        if (!appointmentsRepository.existsById(id)) {
            throw new AppointmentNotFoundException("Cannot delete. Appointment with ID " + id + " not found.");
        }
        appointmentsRepository.deleteById(id);
        logger.info("Successfully deleted appointment {}", id);
    }

    private List<ServiceCatalogueResponse> fetchAndVerifyServices(List<UUID> serviceIds) {
        List<ServiceCatalogueResponse> fetchedServices = Flux.fromIterable(serviceIds)
                .parallel()
                .flatMap(this::fetchServiceDetails)
                .collectSortedList(Comparator.comparing(ServiceCatalogueResponse::getName))
                .block();

        if (fetchedServices == null || fetchedServices.size() != serviceIds.size()) {
            throw new AppointmentCreationException("Could not retrieve details for all requested services.");
        }
        return fetchedServices;
    }

    private Mono<ServiceCatalogueResponse> fetchServiceDetails(UUID serviceId) {
        logger.info("Fetching details for service ID: {}", serviceId);
        return catalogueServiceWebClient.get()
                .uri("/services/{id}", serviceId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new AppointmentCreationException("Service with ID " + serviceId + " not found in catalogue.")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("Service catalogue is currently unavailable.")))
                .bodyToMono(ServiceCatalogueResponse.class)
                .onErrorResume(ex -> {
                    logger.error("Error fetching service details for ID {}: {}", serviceId, ex.getMessage());
                    return Mono.error(ex);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public SlotsResponse getBookedSlots(LocalDate date) {

        logger.info("Fetching booked slots for date: {}", date);
        try {
            List<Appointment> appointments =
                    appointmentsRepository.findByDateTimeBetween(
                            date.atStartOfDay(),
                            date.plusDays(1).atStartOfDay()
                    );
            logger.info("Found {} appointment(s) on {}", appointments.size(), date);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            Set<String> booked = new HashSet<>();

            for (Appointment appointment : appointments) {

                LocalDateTime start = appointment.getDateTime();

                int totalMinutes = appointment.getServices()
                        .stream()
                        .mapToInt(ServiceModel::getDurationInMinutes)
                        .sum();

                LocalDateTime end = start.plusMinutes(totalMinutes);
                logger.debug(
                        "Processing appointment {} — start: {}, duration: {} mins, end: {}",
                        appointment.getId(), start, totalMinutes, end
                );
                LocalDateTime slot = start;

                while (!slot.isAfter(end.minusMinutes(30))) {
                    String slotString = slot.toLocalTime().format(fmt);
                    booked.add(slotString);
                    logger.debug("Marked booked slot: {}", slotString);
                    slot = slot.plusMinutes(30);
                }
            }
            List<String> sorted = booked.stream().sorted().toList();

            logger.info(
                    "Completed booked slot calculation for {} — total booked intervals: {}",
                    date, sorted.size()
            );

            return new SlotsResponse(sorted);

        } catch (DataAccessException dae) {
            logger.error("Database error while retrieving booked slots for {}: {}", date, dae.getMessage(), dae);
            throw new ServiceUnavailableException("Database error while retrieving booked slots.", dae);
        } catch (Exception ex) {
            logger.error("Unexpected error retrieving booked slots for {}: {}", date, ex.getMessage(), ex);
            throw new AppointmentRetrievalException("Error retrieving booked slots for date " + date, ex);
        }
    }

}