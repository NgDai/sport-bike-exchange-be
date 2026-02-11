package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.dto.request.UserUpdateRequest;
import com.bicycle.marketplace.dto.response.UserResponse;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.enums.Role;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    IUserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    public Users createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        Users user = userMapper.toUser(request);
        //user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.BUYER.name());

        user.setRole(roles);
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Users> getAllUser() {
        return userRepository.findAll();
    }

    @PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")
    public UserResponse getUserById(int userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserResponse updateUser(int userId, UserUpdateRequest request) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(int userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        return "User deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deActiveUser(int userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getStatus().matches("Inactive")){
            user.setStatus("Active");
        }else{
            user.setStatus("Inactive");
        }
        return userMapper.toUserResponse(userRepository.save(user));
    }
}
