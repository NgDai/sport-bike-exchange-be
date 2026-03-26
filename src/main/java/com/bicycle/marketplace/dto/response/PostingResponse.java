package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PostingResponse {
    // BikeListing fields
    int listingId;
    String title;
    String description;
    String image_url;
    double price;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Seller info
    String sellerName;


    // Bicycle basic info
    int bikeId;
    String brandName;
    String categoryName;

    // Bicycle specs
    String bikeType;
    String wheelSize;
    String numberOfGears;
    String brakeType;
    int yearManufacture;
    String frameSize;
    String drivetrain;
    String forkType;
    String color;
    String frameMaterial;
    String condition;
    Double weight;
    String saddle;
    String chainring;
    String chain;
    String handlebar;
    String rim;
    String shockAbsorber;
}
