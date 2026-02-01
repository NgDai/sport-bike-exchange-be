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

public class DepositSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int settlementId;
    @ManyToOne
    @JoinColumn(name = "deposit_id")
    Deposit deposit;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    Users receiver;
    double amount;
    String reason;
    @CreationTimestamp
    Date createAt;
}
