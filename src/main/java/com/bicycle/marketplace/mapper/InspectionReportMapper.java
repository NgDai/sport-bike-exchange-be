package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InspectionReportMapper {
    InspectionReportResponse toInspectorReportResponse(InspectionReport inspectionReport);
    InspectionReport toInspectionReport(InspectionReportCreationRequest request);
    void updateInspectionReport(@MappingTarget InspectionReport inspectionReport, InspectionReportUpdateRequest request);
}
