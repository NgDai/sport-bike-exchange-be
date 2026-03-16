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
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int depositId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    Users user;
    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = true)
    BikeListing listing;
    @ManyToOne
    @JoinColumn(name = "event_bike_id", nullable = true)
    EventBicycle eventBicycle;
    String type;
    double amount;
    String status;
    @CreationTimestamp
    Date createdAt;
}
