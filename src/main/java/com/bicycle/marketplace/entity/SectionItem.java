package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "section_items")
public class SectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", unique = true, nullable = false)
    private Long itemId;

    @Column(name = "seller_checked_in")
    private Boolean sellerCheckedIn;

    @Column(name = "buyer_checked_in")
    private Boolean buyerCheckedIn;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Sections sections;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private OrderDetail orderDetail;

    @OneToOne(mappedBy = "sectionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inspection inspection;

    @OneToOne(mappedBy = "sectionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Result result;

    @OneToMany(mappedBy = "sectionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<PaymentItem> paymentItems;
}
