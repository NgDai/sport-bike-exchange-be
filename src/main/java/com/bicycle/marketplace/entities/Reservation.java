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
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int reservationId;
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    Users buyer;

    @ManyToOne
    @JoinColumn(name = "bike_listing_id")
    BikeListing bikeListing;
    String status;
    @CreationTimestamp
    Date createdAt;
}
