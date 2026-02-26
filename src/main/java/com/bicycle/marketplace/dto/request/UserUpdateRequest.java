package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.Email;
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
public class UserUpdateRequest {

    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    @Size(max = 100, message = "FULL_NAME_MAX_LENGTH")
    @Pattern(regexp = "^[\\p{L}\\s.'\\-]*$", message = "FULL_NAME_NO_NUMBERS")
    private String fullName;

    @Email(message = "EMAIL_INVALID")
    @Size(max = 100)
    private String email;

    @Size(max = 20, message = "PHONE_MAX_LENGTH")
    @Pattern(regexp = "^(|[+]?[0-9\\s()-]{10,20})$", message = "PHONE_INVALID_FORMAT")
    private String phone;

    private String avatar;

    private String address;
}
