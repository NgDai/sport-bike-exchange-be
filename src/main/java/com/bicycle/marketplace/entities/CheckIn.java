package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class CheckIn {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    int checkInId;
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    Users buyer;
    @ManyToOne
    @JoinColumn(name = "seller_id")
    Users seller;
    @ManyToOne
    @JoinColumn(name = "event_bike_id")
    Events eventBikeId;
    String token;
    String status;
}
