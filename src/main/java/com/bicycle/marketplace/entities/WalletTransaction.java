package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int walletTransId;
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    Wallet wallet;
    double amount;
    String type;
    String description;
    @CreationTimestamp
    Date createAt;
}
