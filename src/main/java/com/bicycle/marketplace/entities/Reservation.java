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
    @JoinColumn(name = "listing_id", nullable = true)
    BikeListing listing;
    @ManyToOne
    @JoinColumn(name = "event_bike_id", nullable = true)
    EventBicycle eventBicycle;
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    Users buyer;
    @ManyToOne
    @JoinColumn(name = "inspector_id")
    Users inspector;

    @Column(name = "meeting_location")
    String meetingLocation;

    @Column(name = "meeting_time")
    Date meetingTime;
    String status; // "Deposited", "Scheduled", "Completed", "Cancelled"
    @OneToOne
    @JoinColumn(name = "deposit_id")
    Deposit deposit;
    Double depositAmount;
    @CreationTimestamp
    Date reservedAt;
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "cancel_description")
    private String cancelDescription;
}
