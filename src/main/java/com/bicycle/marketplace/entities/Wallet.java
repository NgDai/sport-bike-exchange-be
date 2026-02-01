package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int walletId;
    @OneToOne
    @JoinColumn(name = "user_id")
    Users user;
    double balance;
    Date lastUpdated;
}
