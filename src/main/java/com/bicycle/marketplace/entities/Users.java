package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String walletBalance;
    private String status;
    @CreationTimestamp
    private LocalDate create_date;
}
