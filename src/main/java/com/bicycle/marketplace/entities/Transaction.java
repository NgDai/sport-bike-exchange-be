package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int transactionId;
    @ManyToOne
    @JoinColumn(name = "event_id")
    Events event;
    @ManyToOne
    @JoinColumn(name = "listing_id")
    BikeListing listing;
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    Users buyer;
    @ManyToOne
    @JoinColumn(name = "seller_id")
    Users seller;
    @OneToOne
    @JoinColumn(name = "deposit_id")
    Deposit deposit;
    @OneToOne
    @JoinColumn(name = "reservation_id")
    Reservation reservation;
    double amount;
    double actualPrice;
    String status;
    @CreationTimestamp
    Date createAt;
    @UpdateTimestamp
    Date updateAt;
}
