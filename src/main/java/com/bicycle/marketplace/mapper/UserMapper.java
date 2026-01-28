package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.UserCreationRequest;
import com.bicycle.marketplace.dto.request.UserUpdateRequest;
import com.bicycle.marketplace.dto.response.UserResponse;
import com.bicycle.marketplace.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreationRequest request);

    UserResponse toUserResponse(Users user);

    void updateUser(@MappingTarget Users user, UserUpdateRequest request);
}
