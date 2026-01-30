package com.bicycle.marketplace.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class Role {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    int roleId;
    String roleName;
}
