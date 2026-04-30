package com.acainfo.intensive.infrastructure.mapper;

import com.acainfo.intensive.application.dto.CreateIntensiveCommand;
import com.acainfo.intensive.application.dto.UpdateIntensiveCommand;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.CreateIntensiveRequest;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.IntensiveResponse;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.UpdateIntensiveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for the Intensive REST layer.
 */
@Mapper(componentModel = "spring")
public interface IntensiveRestMapper {

    CreateIntensiveCommand toCommand(CreateIntensiveRequest request);

    UpdateIntensiveCommand toCommand(UpdateIntensiveRequest request);

    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "availableSeats", expression = "java(intensive.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(intensive.getMaxCapacity())")
    @Mapping(target = "pricePerHour", expression = "java(intensive.getEffectivePricePerHour())")
    @Mapping(target = "isOpen", expression = "java(intensive.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(intensive.canEnroll())")
    IntensiveResponse toResponse(Intensive intensive);

    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "id", source = "intensive.id")
    @Mapping(target = "name", source = "intensive.name")
    @Mapping(target = "subjectId", source = "intensive.subjectId")
    @Mapping(target = "teacherId", source = "intensive.teacherId")
    @Mapping(target = "status", source = "intensive.status")
    @Mapping(target = "currentEnrollmentCount", source = "intensive.currentEnrollmentCount")
    @Mapping(target = "capacity", source = "intensive.capacity")
    @Mapping(target = "startDate", source = "intensive.startDate")
    @Mapping(target = "endDate", source = "intensive.endDate")
    @Mapping(target = "createdAt", source = "intensive.createdAt")
    @Mapping(target = "updatedAt", source = "intensive.updatedAt")
    @Mapping(target = "availableSeats", expression = "java(intensive.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(intensive.getMaxCapacity())")
    @Mapping(target = "pricePerHour", expression = "java(intensive.getEffectivePricePerHour())")
    @Mapping(target = "isOpen", expression = "java(intensive.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(intensive.canEnroll())")
    IntensiveResponse toEnrichedResponse(
            Intensive intensive,
            String subjectName,
            String subjectCode,
            String teacherName
    );
}
