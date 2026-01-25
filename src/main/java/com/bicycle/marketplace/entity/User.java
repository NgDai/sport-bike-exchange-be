package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private int userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone")
    private int phone;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "wallet_balance")
    private float walletBalance;

    @Column(name = "reputation_score")
    private  String reputaionScore;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
