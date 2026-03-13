package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "listing.listingId", target = "listingId")
    @Mapping(source = "listing.title", target = "listingTitle")
    @Mapping(source = "buyer.userId", target = "buyerId")
    @Mapping(source = "buyer.fullName", target = "buyerName")
    @Mapping(source = "deposit.depositId", target = "depositId")

    // Map các trường mới
    @Mapping(source = "inspector.userId", target = "inspectorId")
    @Mapping(source = "inspector.fullName", target = "inspectorName")
    @Mapping(source = "inspector.phone", target = "inspectorPhone")
    ReservationResponse toReservationResponse(Reservation reservation);

    void updateReservation(@MappingTarget Reservation reservation, ReservationUpdateRequest request);

}