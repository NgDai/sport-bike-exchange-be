package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InspectionReportMapper {
    @Mapping(source = "reservation.reservationId", target = "reservationId")
    @Mapping(source = "inspector.userId", target = "inspectorId")
    InspectionReportResponse toInspectorReportResponse(InspectionReport inspectionReport);

    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "inspector", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InspectionReport toInspectionReport(InspectionReportCreationRequest request);
    void updateInspectionReport(@MappingTarget InspectionReport inspectionReport, InspectionReportUpdateRequest request);
}
