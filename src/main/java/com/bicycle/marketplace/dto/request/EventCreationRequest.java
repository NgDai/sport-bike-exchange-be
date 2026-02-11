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
    private String bikeType;
    private String location;
    private String address;
    private LocalDate createDate;
    private LocalDate publicDate;
    private LocalDate updateDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private double sellerDepositRate;
    private double buyerDepositRate;
    private double platformFeeRate;
    private String status;
}
