package com.bicycle.marketplace.dto.request.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AuthenticationRequest {
    String username;
    String password;
}
