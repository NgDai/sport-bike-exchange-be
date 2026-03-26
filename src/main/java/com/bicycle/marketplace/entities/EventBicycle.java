package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventBicycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int eventBikeId;
    @ManyToOne
    @JoinColumn(name = "event_id")
    Events event;
    @OneToOne
    @JoinColumn(name = "listing_id", nullable = true)
    BikeListing listing;
    @ManyToOne
    @JoinColumn(name = "bike_id")
    Bicycle bicycle;
    @ManyToOne
    @JoinColumn(name = "user_id")
    Users seller;
    String sellerName;
    String bikeType;
    @Column(nullable = true)
    Double price;
    String condition;
    String image_url;
    String title;
    String status;
    @CreationTimestamp
    LocalDate createDate;
    @CreationTimestamp
    LocalDate updateDate;
}
