package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DisputeMapper {
    @Mapping(source = "transaction.transactionId", target = "transactionId")
    @Mapping(source = "raisedBy.username", target = "raisedBy")
    @Mapping(source = "raisedBy.userId", target = "raisedByUserId")
    @Mapping(source = "assignedInspector.userId", target = "assignedInspectorId")
    @Mapping(source = "assignedInspector.fullName", target = "assignedInspectorName")
    @Mapping(source = "createdAt", target = "createdAt")
    DisputeResponse toDisputeResponse(Dispute dispute);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "raisedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedInspector", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Dispute toDispute(DisputeCreationRequest request);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "raisedBy", ignore = true)
    @Mapping(target = "assignedInspector", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateDispute(@MappingTarget Dispute dispute, DisputeUpdateRequest request);
}
