package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private IUserRepository userRepository;

    public User createUser(UserCreationRequest request) {
        User user = new User();

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
}
