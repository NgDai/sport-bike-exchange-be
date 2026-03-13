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
    @Mapping(source = "dispute.disputeId", target = "disputeId")
    @Mapping(source = "inspector.userId", target = "inspectorId")
    InspectionReportResponse toInspectorReportResponse(InspectionReport inspectionReport);

    @Mapping(target = "dispute", ignore = true)
    @Mapping(target = "inspector", ignore = true)
    InspectionReport toInspectionReport(InspectionReportCreationRequest request);
    void updateInspectionReport(@MappingTarget InspectionReport inspectionReport, InspectionReportUpdateRequest request);
}
