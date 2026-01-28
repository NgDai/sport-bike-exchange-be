package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DepositSettlement")
public class DepositSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlementId", unique = true, nullable = false)
    private Integer settlementId;

    @Column(name = "depositId")
    private Integer depositId;

    @Column(name = "receiverId")
    private Integer receiverId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "settledAt")
    private LocalDateTime settledAt;
}
