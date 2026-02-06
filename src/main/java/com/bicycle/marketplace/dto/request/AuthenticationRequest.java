package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class AuthenticationRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    String password;
}
