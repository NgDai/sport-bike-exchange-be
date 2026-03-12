package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdateRequest {
    private Integer eventId;
    private Integer listingId;
    private Integer buyerId;
    private Integer sellerId;
    private Integer depositId;
    private Integer reservationId;
    private Double amount;
    private Double actualPrice;
    private Double fee;
}
