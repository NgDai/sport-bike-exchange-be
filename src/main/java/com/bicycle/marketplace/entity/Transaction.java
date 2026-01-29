package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionId", unique = true, nullable = false)
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BikeListing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User seller;

    @Column(name = "finalPrice")
    private Double finalPrice;

    @Column(name = "status")
    private String status;

    @Column(name = "completedAt")
    private LocalDateTime completedAt;
}
