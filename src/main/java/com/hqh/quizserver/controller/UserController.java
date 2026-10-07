package com.hqh.quizserver.controller;

import com.hqh.quizserver.dto.OnUpdate;
import com.hqh.quizserver.dto.UserDTO;
import com.hqh.quizserver.dto.UserRequestDTO;
import com.hqh.quizserver.exceptions.domain.user.UserNotFoundException;
import com.hqh.quizserver.exceptions.domain.user.UsernameOrEmailExistException;
import com.hqh.quizserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hqh.quizserver.constant.DomainConstant.USER_DELETED_SUCCESSFULLY;
import static com.hqh.quizserver.constant.MessageTypeConstant.MESSAGE_SUCCESS;
import static com.hqh.quizserver.utils.ResponseUtils.response;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> addNewUser(@Valid @RequestBody UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        return new ResponseEntity<>(userService.addNewUser(request), CREATED);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return new ResponseEntity<>(userService.getUsers(), OK);
    }

    @GetMapping("/find/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) throws UserNotFoundException {
        return new ResponseEntity<>(userService.findUserById(id), OK);
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@Validated(OnUpdate.class) @RequestBody UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        return new ResponseEntity<>(userService.updateUser(request), OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) throws UserNotFoundException {
        userService.deleteUser(id);
        return response(OK, MESSAGE_SUCCESS, USER_DELETED_SUCCESSFULLY);
    }

}
