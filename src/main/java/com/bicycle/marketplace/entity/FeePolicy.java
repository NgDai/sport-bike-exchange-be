package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fee_policies")
public class FeePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_id", unique = true, nullable = false)
    private Long feeId;

    @Column(name = "fee_type", nullable = false)
    private String feeType;

    @Column(name = "value", nullable = false)
    private Float value;

    @OneToMany(mappedBy = "feePolicy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Posting> postings;
}
