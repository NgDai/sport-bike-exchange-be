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
    private Integer buyerId;
    private Integer depositId;
    private Integer eventId;
    private Integer reservationId;
    private Integer sellerId;
    private Integer listingId;
    private double amount;
    private double actualPrice;
    private Date createdAt;
    private Date updatedAt;
    private String status;
}
