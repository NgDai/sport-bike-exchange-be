package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", unique = true, nullable = false)
    private Long paymentId;

    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "status")
    private String status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private OrderDetail orderDetail;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentItem> paymentItems;
}
