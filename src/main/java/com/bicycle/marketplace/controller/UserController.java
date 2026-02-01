package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.dto.request.UserUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.UserResponse;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    ApiResponse<Users> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<Users> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Users>> getUsers() {
        ApiResponse<List<Users>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getAllUser());
        return apiResponse;
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable int userId) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUserById(userId));
        return apiResponse;
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable int userId, @RequestBody UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(userId, request));
        return apiResponse;
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable int userId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.deleteUser(userId));
        return apiResponse;
    }

    @PutMapping("/deactivate/{userId}")
    ApiResponse<UserResponse> deActiveUser(@PathVariable int userId) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.deActiveUser(userId));
        return apiResponse;
    }
}
