package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponse {
    private int userId;
    private String avatar;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String status;
    private Set<String> role;
    @CreationTimestamp
    private LocalDate create_date;
}
