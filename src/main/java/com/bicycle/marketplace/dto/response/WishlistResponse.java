package com.bicycle.marketplace.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistResponse {
    int wishlistId;
    int userId;
    int listingId;
    String listingTitle;
    String listingDescription;
    String listingImageUrl;
    double listingPrice;
    String listingCondition;
    String listingStatus;
    String sellerName;
}
