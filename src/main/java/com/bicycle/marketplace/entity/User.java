package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@AllArgsConstructor

public class User implements Serializable {
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

    public User() {
    }

    public User(String user_id, String username, String password, String fullname, String phone, String address, String date_of_birth, String id_card, boolean is_active, String role, String email) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
        this.date_of_birth = date_of_birth;
        this.id_card = id_card;
        this.is_active = is_active;
        this.role = role;
        this.email = email;
    }


}
