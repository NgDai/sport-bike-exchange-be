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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "title")
    private String title;

    @Column(name = "model")
    private String model;

    @Column(name = "frame_size")
    private String frameSize;

    @Column(name = "wheel_size")
    private String wheelSize;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "brake_type")
    private String brakeType;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}