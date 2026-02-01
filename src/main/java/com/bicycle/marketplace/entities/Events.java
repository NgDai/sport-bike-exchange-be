package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int eventId;
    @ManyToOne
    @JoinColumn(name = "create_by")
    private Users creator;
    private String name;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private double sellerDepositRate;
    private double buyerDepositRate;
    private double platformFeeRate;
    private String status;
}
