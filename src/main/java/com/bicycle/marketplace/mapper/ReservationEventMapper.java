package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationEventMapper {
    @Mapping(source = "eventBicycle.eventBikeId", target = "eventBikeId")
    @Mapping(source = "eventBicycle.title", target = "eventBikeTitle")
    @Mapping(source = "buyer.userId", target = "buyerId")
    @Mapping(source = "buyer.fullName", target = "buyerName")
    @Mapping(source = "eventBicycle.image_url", target = "eventBikeImage")
    @Mapping(source = "eventBicycle.seller.userId", target = "sellerId")
    @Mapping(source = "eventBicycle.seller.fullName", target = "sellerName")
    @Mapping(source = "inspector.userId", target = "inspectorId")
    @Mapping(source = "inspector.fullName", target = "inspectorName")
    @Mapping(source = "inspector.phone", target = "inspectorPhone")
    @Mapping(source = "deposit.depositId", target = "depositId")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "cancelDescription", target = "cancelDescription")
    @Mapping(source = "cancelImage", target = "cancelImage")
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "listingId", ignore = true)
    @Mapping(target = "listingTitle", ignore = true)
    @Mapping(target = "listingImage", ignore = true)
    ReservationResponse toReservationResponse(Reservation reservation);
}
