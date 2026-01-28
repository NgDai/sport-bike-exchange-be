package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults (level = lombok.AccessLevel.PRIVATE)
@Builder
public class AuthenticationRequest {
    String username;
    String password;
}
