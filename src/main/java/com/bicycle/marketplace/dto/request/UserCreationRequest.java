package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreationRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 50, message = "USERNAME_INVALID_LENGTH")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "USERNAME_INVALID_FORMAT")
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 100, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(max = 100, message = "FULL_NAME_MAX_LENGTH")
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "PHONE_REQUIRED")
    @Size(min = 10, max = 20, message = "PHONE_MAX_LENGTH")
    @Pattern(regexp = "^[+]?[0-9\\s()-]{10,20}$", message = "PHONE_INVALID_FORMAT")
    private String phone;

    private String avatar;
    private String status;
    private String address;
}
