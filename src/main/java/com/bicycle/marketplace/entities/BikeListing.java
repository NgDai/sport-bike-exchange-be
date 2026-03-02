package com.bicycle.marketplace.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bike_listing")
public class BikeListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "listing_id")
    @JsonIgnore
    private int listingId;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JsonIgnore
    private Users seller;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Events event;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "bicycle_id", nullable = true)
    @JsonIgnore
    private Bicycle bicycle;

    private String title;
    private String brand;
    private String model;
    private String category;

    @Column(name = "frame_size")
    private String frameSize;

    @Column(name = "wheel_size")
    private String wheelSize;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "brake_type")
    private String brakeType;

    private String transmission;
    private Double weight;

    @Column(name = "image_url")
    private String imageUrl;

    private String description;
    private Double price;
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
