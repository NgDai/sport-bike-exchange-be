package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DepositSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlementId", unique = true, nullable = false)
    private Integer settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depositId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Deposit deposit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiverId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User receiver;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "settledAt")
    private LocalDateTime settledAt;
}
