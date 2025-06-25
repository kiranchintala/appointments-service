package com.mtbs.appointments.mapper;

import com.mtbs.appointments.dto.*;
import com.mtbs.appointments.model.Appointment;
import com.mtbs.appointments.model.ServiceModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper interface for converting between DTOs and JPA entities for Appointments and Services.
 * 'componentModel = "spring"' makes MapStruct generate a Spring component,
 * so it can be injected and used as a Spring bean.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper {


    // New method to map the response from the catalogue service to our local ServiceModel entity
    @Mapping(target = "serviceCatalogueId", source = "id")
    @Mapping(target = "id", ignore = true)
    ServiceModel toServiceModel(ServiceCatalogueResponse serviceCatalogueResponse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "services", ignore = true)
    Appointment toEntity(CreateAppointmentRequest request);

    AppointmentResponse toResponseDto(Appointment appointment);

    List<AppointmentResponse> toDtoList(List<Appointment> appointments);

    ServiceDTO toServiceDto(ServiceModel serviceModel);

}

