package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.Email;
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
    private String fullName;

    @Email(message = "EMAIL_INVALID")
    @Size(max = 100)
    private String email;

    @Size(max = 20, message = "PHONE_MAX_LENGTH")
    private String phone;

    @Size(max = 50, message = "STATUS_MAX_LENGTH")
    private String status;
}
