package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toTransactionResponse(Transaction transaction);
    Transaction toTransaction(TransactionCreationRequest request);
    void updateTransaction(@MappingTarget Transaction transaction, TransactionUpdateRequest request);
}
