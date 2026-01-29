package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "walletTransId", unique = true, nullable = false)
    private Integer walletTransId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walletId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Wallet wallet;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
