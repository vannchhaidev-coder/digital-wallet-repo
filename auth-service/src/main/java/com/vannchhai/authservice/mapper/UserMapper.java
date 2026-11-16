package com.vannchhai.authservice.mapper;

import com.vannchhai.authservice.dto.response.UserResponse;
import com.vannchhai.authservice.model.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(UserEntity entity);
}
