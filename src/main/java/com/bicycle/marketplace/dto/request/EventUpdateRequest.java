package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class EventUpdateRequest {
    private String name;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
//    private double sellerDepositRate;
//    private double buyerDepositRate;
//    private double platformFeeRate;
    private String status;
}
