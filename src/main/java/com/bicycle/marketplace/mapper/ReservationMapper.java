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
    @Mapping(source = "listing.image_url", target = "listingImage")
    @Mapping(source = "listing.seller.userId", target = "sellerId")
    @Mapping(source = "listing.seller.fullName", target = "sellerName")
    @Mapping(source = "inspector.userId", target = "inspectorId")
    @Mapping(source = "inspector.fullName", target = "inspectorName")
    @Mapping(source = "inspector.phone", target = "inspectorPhone")
    @Mapping(source = "deposit.depositId", target = "depositId")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "cancelDescription", target = "cancelDescription")
    @Mapping(source = "cancelImage", target = "cancelImage")
    @Mapping(source = "eventBicycle.eventBikeId", target = "eventBikeId")
    @Mapping(source = "eventBicycle.title", target = "eventBikeTitle")
    @Mapping(source = "eventBicycle.image_url", target = "eventBikeImage")
    @Mapping(target = "remainingAmount", ignore = true)
    ReservationResponse toReservationResponse(Reservation reservation);

    void updateReservation(@MappingTarget Reservation reservation, ReservationUpdateRequest request);
}