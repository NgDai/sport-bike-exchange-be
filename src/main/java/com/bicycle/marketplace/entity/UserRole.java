package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "UserRole")
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

    @Id
    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Id
    @Column(name = "roleId", nullable = false)
    private Integer roleId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        private Integer userId;
        private Integer roleId;
    }
}
