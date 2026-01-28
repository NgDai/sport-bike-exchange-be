package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "walletId", unique = true, nullable = false)
    private Integer walletId;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "balance")
    private Double balance;

    @Column(name = "update_date")
    private LocalDateTime updateDate;
}
