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
    DisputeResponse toDisputeResponse(Dispute dispute);
    void updateDispute(@MappingTarget Dispute dispute, DisputeUpdateRequest request);
}
