package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private int transactionId;
    private int buyerId;
    private String buyerName;
    private int depositId;
    private int eventId;
    private int reservationId;
    private int sellerId;
    private String sellerName;
    private int listingId;
    private String listingTitle; // Thêm
    private String listingImage;
    private int eventBicycleId;
    private String eventBicycleTitle; // Thêm
    private String eventBicycleImage;

    // Bicycle basic info
    private int bikeId;
    private String brandName;
    private String categoryName;

    // Bicycle specs
    private String bikeType;
    private String wheelSize;
    private String numberOfGears;
    private String brakeType;
    private int yearManufacture;
    private String frameSize;
    private String drivetrain;
    private String forkType;
    private String color;
    private String frameMaterial;
    private String condition;
    private Double weight;
    private String saddle;
    private String chainring;
    private String chain;
    private String handlebar;
    private String rim;
    private String shockAbsorber;

    private String inspectorName;
    private String inspectorPhone;
    private String meetingLocation;
    private Date meetingTime;

    private double amount;
    private double actualPrice;
    private double fee;
    private String description;
    private String type;
    private Date createdAt;
    private Date updatedAt;
    private String status;
}
