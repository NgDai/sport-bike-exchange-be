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
    private double amount;
    private double actualPrice;
    private double fee;
    private String description;
    private String type;
    private Date createdAt;
    private Date updatedAt;
    private String status;
}
