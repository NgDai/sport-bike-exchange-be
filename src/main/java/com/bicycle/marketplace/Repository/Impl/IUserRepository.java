package com.bicycle.marketplace.Repository.Impl;

import com.bicycle.marketplace.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    void createUser(User user);
    List<User> listAllUsers();
    User findUserById(int userId);
    void updateUser(User user);
    void deleteUser(int userId);
}
