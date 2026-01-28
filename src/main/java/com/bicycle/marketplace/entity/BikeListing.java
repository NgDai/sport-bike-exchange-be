package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bike_listing")
public class BikeListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer listingId;
    private Integer sellerId;
    private Integer eventId;
    private String title;
    private String brand;
    private String model;
    private String category;
    private String frameSize;
    private String wheelSize;
    private Integer manufactureYear;
    private String brakeType;
    private String transmission;
    private Double weight;
    @Column(columnDefinition = "text")
    private String imageUrl;
    @Column(columnDefinition = "text")
    private String description;
    private Double price;
    private String status;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
