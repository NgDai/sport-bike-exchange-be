package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationId", unique = true, nullable = false)
    private Integer reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BikeListing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depositId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Deposit deposit;

    @Column(name = "status")
    private String status;

    @Column(name = "reservedAt")
    private LocalDateTime reservedAt;
}
