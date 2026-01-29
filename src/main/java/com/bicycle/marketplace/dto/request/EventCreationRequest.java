package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreationRequest {
    private String name;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private double sellerDepositRate;
    private double buyerDepositRate;
    private double platformFeeRate;
    private String status;
}
