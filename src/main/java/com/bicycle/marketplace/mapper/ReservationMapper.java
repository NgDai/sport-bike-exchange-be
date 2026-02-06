package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponse toReservationResponse(Reservation reservation);
    void updateReservation(@MappingTarget Reservation reservation, ReservationUpdateRequest request);
}
