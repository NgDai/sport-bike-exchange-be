package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.DepositSettlementCreationRequest;
import com.bicycle.marketplace.dto.request.DepositSettlementUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositSettlementResponse;
import com.bicycle.marketplace.entities.DepositSettlement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepositSettlementMapper {
    DepositSettlementResponse toDepositSettlementResponse(DepositSettlement depositSettlement);
    void updateDepositSettlement(@MappingTarget DepositSettlement depositSettlement, DepositSettlementUpdateRequest request);
}
