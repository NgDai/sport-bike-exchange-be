package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkInId", unique = true, nullable = false)
    private Integer checkInId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event event;

    @Column(name = "role")
    private String role;

    @Column(name = "checkInTime")
    private LocalDateTime checkInTime;
}
