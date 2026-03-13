package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bike_listing")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BikeListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int listingId;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bicycle_id")
    Bicycle bicycle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    Users seller;
    String title;
    String description;
    String image_url;
    double price;
    String condition;
    String status; // Available, Deposited, Sold, Pending, Waiting_Payment
    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;
}
