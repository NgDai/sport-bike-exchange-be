package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionId", unique = true, nullable = false)
    private Integer transactionId;

    @Column(name = "listingId")
    private Integer listingId;

    @Column(name = "buyerId")
    private Integer buyerId;

    @Column(name = "sellerId")
    private Integer sellerId;

    @Column(name = "finalPrice")
    private Double finalPrice;

    @Column(name = "status")
    private String status;

    @Column(name = "completedAt")
    private LocalDateTime completedAt;
}
