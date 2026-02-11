package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.DepositUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.entities.Deposit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepositMapper {
    DepositResponse toDepositResponse(Deposit deposit);
    Deposit toDeposit(DepositCreationRequest request);
    void updateDeposit(@MappingTarget Deposit deposit, DepositUpdateRequest request);
}
