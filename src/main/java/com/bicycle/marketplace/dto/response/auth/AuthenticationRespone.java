package com.bicycle.marketplace.dto.response.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AuthenticationRespone {
    String token;
    boolean authenticated;
}
