package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.entities.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "listing.listingId", target = "listingId")
    @Mapping(source = "listing.title", target = "listingTitle")
    @Mapping(source = "listing.description", target = "listingDescription")
    @Mapping(source = "listing.image_url", target = "listingImageUrl")
    @Mapping(source = "listing.price", target = "listingPrice")
    @Mapping(source = "listing.condition", target = "listingCondition")
    @Mapping(source = "listing.status", target = "listingStatus")
    @Mapping(source = "listing.seller.fullName", target = "sellerName")
    WishlistResponse toWishlistResponse(Wishlist wishlist);
}
