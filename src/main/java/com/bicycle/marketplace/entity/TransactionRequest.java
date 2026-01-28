package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TransactionRequest")
public class TransactionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestId", unique = true, nullable = false)
    private Integer requestId;

    @Column(name = "listingId")
    private Integer listingId;

    @Column(name = "buyerId")
    private Integer buyerId;

    @Column(name = "depositId")
    private Integer depositId;

    @Column(name = "status")
    private String status;

    @Column(name = "requestAt")
    private LocalDateTime requestAt;
}
