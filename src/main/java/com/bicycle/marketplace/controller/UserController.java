package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.dto.request.UserUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entity.Users;
import com.bicycle.marketplace.service.UserService;
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
        apiResponse.setMessage("User created successfully");
        return apiResponse;
    }

    @GetMapping
    List<Users> getUsers() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    Users getUserById(@PathVariable int userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    Users updateUser(@PathVariable int userId, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }
    @PutMapping("/deactivate/{userId}")
    Users deActiveUser(@PathVariable int userId) {
        return userService.deActiveUser(userId);
    }
}
