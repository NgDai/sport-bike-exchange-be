package com.bicycle.marketplace.entity;

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
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int disputeId;
    @OneToOne
    @JoinColumn(name = "transaction_id")
    Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "raised_by")
    Users raiser;
    String reason;
    String status;
    @CreationTimestamp
    Date createAt;
}
