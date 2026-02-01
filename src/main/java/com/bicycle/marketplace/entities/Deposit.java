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
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int depositId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    Users user;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    BikeListing listing;
    String type;
    double amount;
    String nvarchar;
    @CreationTimestamp
    Date createAt;
}
