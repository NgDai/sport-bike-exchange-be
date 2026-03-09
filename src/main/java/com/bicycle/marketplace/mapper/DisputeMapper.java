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
    DisputeResponse toDisputeResponse(Dispute dispute);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "raisedBy", ignore = true)
    Dispute toDispute(DisputeCreationRequest request);
    void updateDispute(@MappingTarget Dispute dispute, DisputeUpdateRequest request);
}
