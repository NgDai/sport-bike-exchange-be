package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.entities.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "listing.listingId", target = "listingId")
    WishlistResponse toWishlistResponse(Wishlist wishlist);
}
