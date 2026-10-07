package com.hqh.quizserver.controller;

import com.hqh.quizserver.dto.UserDTO;
import com.hqh.quizserver.dto.UserLoginRequestDTO;
import com.hqh.quizserver.dto.UserLoginResponseDTO;
import com.hqh.quizserver.dto.UserRegisterRequestDTO;
import com.hqh.quizserver.exceptions.domain.user.UserNotFoundException;
import com.hqh.quizserver.exceptions.domain.user.UsernameOrEmailExistException;
import com.hqh.quizserver.services.AuthService;
import com.hqh.quizserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService,
                          AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegisterRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        return new ResponseEntity<>(userService.register(request), CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        return ResponseEntity.status(OK).body(authService.login(userLoginRequestDTO));
    }

}
