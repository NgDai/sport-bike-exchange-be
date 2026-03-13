package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "buyer.userId", target = "buyerId", defaultExpression = "java(0)")
    @Mapping(source = "seller.userId", target = "sellerId", defaultExpression = "java(0)")
    @Mapping(source = "event.eventId", target = "eventId", defaultExpression = "java(0)")
    @Mapping(source = "listing.listingId", target = "listingId", defaultExpression = "java(0)")
    @Mapping(source = "listing.title", target = "listingTitle", defaultExpression = "java(\"\")") // Thêm
    @Mapping(source = "listing.image_url", target = "listingImage", defaultExpression = "java(\"\")") // Thêm
    @Mapping(source = "deposit.depositId", target = "depositId", defaultExpression = "java(0)")
    @Mapping(source = "reservation.reservationId", target = "reservationId", defaultExpression = "java(0)")
    @Mapping(source = "createAt", target = "createdAt")
    @Mapping(source = "updateAt", target = "updatedAt")
    @Mapping(target = "description", source = "description", defaultExpression = "java(\"\")")
    @Mapping(target = "type", source = "type", defaultExpression = "java(\"\")")
    @Mapping(target = "status", source = "status", defaultExpression = "java(\"\")")
    TransactionResponse toTransactionResponse(Transaction transaction);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    void updateTransaction(@MappingTarget Transaction transaction, TransactionUpdateRequest request);
}