package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int wishlistId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private BikeListing listing;
}
