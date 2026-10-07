package com.hqh.boilerplate.services;

import com.hqh.boilerplate.dto.UserLoginRequestDTO;
import com.hqh.boilerplate.dto.UserLoginResponseDTO;

public interface AuthService {
    UserLoginResponseDTO login(UserLoginRequestDTO userLoginRequestDTO);
}
