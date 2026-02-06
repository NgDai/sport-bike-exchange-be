package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private int transactionId;
    private int buyerId;
    private int depositId;
    private int reservationId;
    private int sellerId;
    private int listingId;
    private double amount;
    private String status;
    private Date createdAt;
    private Date completedAt;
}
