package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String bikeType;
    private String location;
    private String address;
    @CreationTimestamp
    private LocalDate createDate;
    @CreationTimestamp
    private LocalDate publicDate;
    @UpdateTimestamp
    private LocalDate updateDate;
    @CreationTimestamp
    private LocalDate startDate;
    private LocalDate endDate;
    private double sellerDepositRate;
    private double buyerDepositRate;
    private double platformFeeRate;
    private String status;
}
