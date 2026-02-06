package com.bicycle.marketplace.dto.response;

import com.bicycle.marketplace.entities.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private int eventId;
    private String createBy;
    private String name;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private double sellerDepositRate;
    private double buyerDepositRate;
    private double platformFeeRate;
    private String status;
}
