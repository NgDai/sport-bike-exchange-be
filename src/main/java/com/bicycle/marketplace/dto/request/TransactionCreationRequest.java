package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreationRequest {
    private Integer eventId;
    private Integer listingId;
    private Integer buyerId;
    private Integer sellerId;
    private Integer depositId;
    private Integer reservationId;
    private Integer eventBikeId;

    private double amount;
    private double actualPrice;
    private Double fee;
}
