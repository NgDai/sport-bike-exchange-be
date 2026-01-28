package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CheckIn")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkInId", unique = true, nullable = false)
    private Integer checkInId;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "eventId")
    private Integer eventId;

    @Column(name = "role")
    private String role;

    @Column(name = "checkInTime")
    private LocalDateTime checkInTime;
}
