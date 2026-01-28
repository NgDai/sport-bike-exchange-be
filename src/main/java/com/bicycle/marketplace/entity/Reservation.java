package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationId", unique = true, nullable = false)
    private Integer reservationId;

    @Column(name = "listingId")
    private Integer listingId;

    @Column(name = "buyerId")
    private Integer buyerId;

    @Column(name = "depositId")
    private Integer depositId;

    @Column(name = "status")
    private String status;

    @Column(name = "reservedAt")
    private LocalDateTime reservedAt;
}
