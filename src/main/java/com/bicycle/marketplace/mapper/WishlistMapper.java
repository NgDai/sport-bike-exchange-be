package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.entities.Wishlist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    WishlistResponse toWishlistResponse(Wishlist wishlist);
}
