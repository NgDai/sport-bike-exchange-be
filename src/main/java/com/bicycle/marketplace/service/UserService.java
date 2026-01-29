package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.dto.request.UserUpdateRequest;
import com.bicycle.marketplace.entity.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private IUserRepository userRepository;

    public Users createUser(UserCreationRequest request) {
        Users user = new Users();

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setWalletBalance(request.getWalletBalance());
        user.setStatus(request.getStatus());
        user.setCreate_date(request.getCreate_date());

        return userRepository.save(user);
    }

    public List<Users> getAllUser(){
        return userRepository.findAll();
    }

    public Users getUserById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Users updateUser(int userId, UserUpdateRequest request) {
        Users user = getUserById(userId);

        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        return userRepository.save(user);
    }

    public void deleteUser(int userId) {
        Users user = getUserById(userId);
        userRepository.delete(user);
    }

    public Users deActiveUser(int userId) {
        Users user = getUserById(userId);
        user.setStatus("Inactive");
        return userRepository.save(user);
    }
}
