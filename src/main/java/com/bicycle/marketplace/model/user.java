package com.bicycle.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class user implements Serializable {
    private String user_id;
    private String username;
    private String password;
    private String fullname;
    private String phone;
    private String address;
    private String date_of_birth;
    private String id_card;
    private boolean is_active;
    private String role;
    private String email;

}
