package com.bicycle.marketplace.dto.response;

import com.bicycle.marketplace.entities.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private int transactionId;
    private int buyerId;
    private int depositId;
    private int eventId;
    private int reservationId;
    private int sellerId;
    private int listingId;
    private double amount;
    private double actualPrice;
    private Date createdAt;
    private Date updateAt;
    private String status;
}
