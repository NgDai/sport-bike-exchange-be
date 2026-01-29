package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

    @Id
    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Id
    @Column(name = "roleId", nullable = false)
    private Integer roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        private Integer userId;
        private Integer roleId;
    }
}
