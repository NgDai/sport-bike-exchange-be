package com.bicycle.marketplace.service;

import com.bicycle.marketplace.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private List<User> userList;

    public UserService(){
        userList = new ArrayList<>();
        String query = "SEKECT u FROM User u";

        userList.add(query)
    }

    public User getUser(int id) {
    }
}
