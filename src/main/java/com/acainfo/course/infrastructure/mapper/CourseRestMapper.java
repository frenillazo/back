package com.acainfo.course.infrastructure.mapper;

import com.acainfo.course.application.dto.CreateCourseCommand;
import com.acainfo.course.application.dto.UpdateCourseCommand;
import com.acainfo.course.domain.model.Course;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.CreateCourseRequest;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.CourseResponse;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.UpdateCourseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for the Course REST layer.
 *
 * <p>Note: {@code currentEnrollmentCount}, {@code availableSeats} and {@code canEnroll}
 * are NOT derived from the domain object — the enricher computes them dynamically from
 * the enrollment table (single source of truth).</p>
 */
@Mapper(componentModel = "spring")
public interface CourseRestMapper {

    CreateCourseCommand toCommand(CreateCourseRequest request);

    UpdateCourseCommand toCommand(UpdateCourseRequest request);

    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "currentEnrollmentCount", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "canEnroll", ignore = true)
    @Mapping(target = "isOpen", expression = "java(course.isOpen())")
    CourseResponse toResponse(Course course);

    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "id", source = "course.id")
    @Mapping(target = "name", source = "course.name")
    @Mapping(target = "subjectId", source = "course.subjectId")
    @Mapping(target = "teacherId", source = "course.teacherId")
    @Mapping(target = "status", source = "course.status")
    @Mapping(target = "capacity", source = "course.capacity")
    @Mapping(target = "pricePerMonth", source = "course.pricePerMonth")
    @Mapping(target = "startDate", source = "course.startDate")
    @Mapping(target = "endDate", source = "course.endDate")
    @Mapping(target = "createdAt", source = "course.createdAt")
    @Mapping(target = "updatedAt", source = "course.updatedAt")
    @Mapping(target = "currentEnrollmentCount", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "canEnroll", ignore = true)
    @Mapping(target = "isOpen", expression = "java(course.isOpen())")
    CourseResponse toEnrichedResponse(
            Course course,
            String subjectName,
            String subjectCode,
            String teacherName
    );
}
