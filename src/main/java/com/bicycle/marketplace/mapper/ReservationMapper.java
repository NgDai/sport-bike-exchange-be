package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "buyer.userId", target = "buyerId")
    @Mapping(source = "listing.listingId", target = "listingId")
    ReservationResponse toReservationResponse(Reservation reservation);

    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "listing", ignore = true)
    Reservation toReservation(ReservationCreationRequest request);
    void updateReservation(@MappingTarget Reservation reservation, ReservationUpdateRequest request);
}
