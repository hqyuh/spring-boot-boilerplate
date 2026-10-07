package com.hqh.quizserver.mapper;

import com.hqh.quizserver.dto.UserDTO;
import com.hqh.quizserver.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    @Mapping(target = "lastLogin", source = "lastLogin")
    @Mapping(target = "joinDate", source = "joinDate")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "active",ignore = true)
    @Mapping(target = "nonLocked", ignore = true)
    @Mapping(target = "password", ignore = true)
    UserDTO toUserResponseDto(User user);

}
