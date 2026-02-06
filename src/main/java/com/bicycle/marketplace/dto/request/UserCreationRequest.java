package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 50, message = "USERNAME_INVALID_LENGTH")
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(max = 100, message = "FULL_NAME_MAX_LENGTH")
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 100)
    private String email;

    @Size(max = 20, message = "PHONE_MAX_LENGTH")
    private String phone;

    private String walletBalance;
    private String status;
    private LocalDate create_date;
}
