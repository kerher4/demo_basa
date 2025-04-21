package com.example.demo.mapper;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserEditDto;
import com.example.demo.dto.UserReadDto;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEditDto userToUserEditDto(User user);

    User userEditDtoToUser(UserEditDto userEditDto);

    UserCreateDto userToUserCreateDto(User user);

    UserReadDto userToUserReadDto(User user);

    User userReadDtoToUser(UserReadDto userReadDto);

    User userCreateDtoToUser(UserCreateDto userCreateDto);

    void updateUserFromUserEditDto(UserEditDto userEditDto, @MappingTarget User user);
}
