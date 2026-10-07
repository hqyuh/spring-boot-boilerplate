package com.hqh.boilerplate.services;

import com.hqh.boilerplate.dto.UserDTO;
import com.hqh.boilerplate.dto.UserRegisterRequestDTO;
import com.hqh.boilerplate.dto.UserRequestDTO;
import com.hqh.boilerplate.entity.User;
import com.hqh.boilerplate.exceptions.domain.user.UserNotFoundException;
import com.hqh.boilerplate.exceptions.domain.user.UsernameOrEmailExistException;

import java.util.List;

public interface UserService {

    UserDTO register(UserRegisterRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException;

    List<UserDTO> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    UserDTO addNewUser(UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException;

    UserDTO updateUser(UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException;

    void deleteUser(Long id) throws UserNotFoundException;

    UserDTO findUserById(Long id) throws UserNotFoundException;

    User getCurrentUser();

}
