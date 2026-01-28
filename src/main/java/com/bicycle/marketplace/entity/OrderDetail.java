package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id", unique = true, nullable = false)
    private Long detailId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posting posting;

    @OneToOne(mappedBy = "orderDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SectionItem sectionItem;

    @OneToOne(mappedBy = "orderDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}
