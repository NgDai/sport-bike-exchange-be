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
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int transactionId;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = true)
    Events event;
    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = true)
    BikeListing listing;
    @ManyToOne
    @JoinColumn(name = "event_bike_id", nullable = true)
    EventBicycle eventBicycle;
    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    Users buyer;
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = true)
    Users seller;
    @OneToOne
    @JoinColumn(name = "deposit_id", nullable = true)
    Deposit deposit;
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = true)
    Reservation reservation;
    double amount;
    double actualPrice;
    double fee;
    String description;
    String type;
    String status; // "Pending", "Paid", "Completed"
    @CreationTimestamp
    Date createAt;
    @UpdateTimestamp
    Date updateAt;
}
