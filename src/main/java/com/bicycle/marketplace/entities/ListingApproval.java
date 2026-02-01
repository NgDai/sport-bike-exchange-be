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

public class ListingApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int approvalId;
    @ManyToOne
    @JoinColumn(name = "listing_id")
    BikeListing listing;

    @ManyToOne
    @JoinColumn(name = "approval_by")
    Users approver;
    String decision;
    String note;
    @CreationTimestamp
    Date createdAt;
}
