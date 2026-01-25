package com.bicycle.marketplace.service.Impl;

import com.bicycle.marketplace.entity.User;

import java.util.List;

public interface IUserService {
    void create(User user);
    void update(User user);
    void delete(int userId);
    User findById(int userId);
    List<User> findAll();
}
