package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreationRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String walletBalance;
    private String status;
    private LocalDate create_date;
}
