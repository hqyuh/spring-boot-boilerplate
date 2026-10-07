package com.hqh.boilerplate.controller;

import com.hqh.boilerplate.dto.OnUpdate;
import com.hqh.boilerplate.dto.UserDTO;
import com.hqh.boilerplate.dto.UserRequestDTO;
import com.hqh.boilerplate.exceptions.domain.user.UserNotFoundException;
import com.hqh.boilerplate.exceptions.domain.user.UsernameOrEmailExistException;
import com.hqh.boilerplate.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hqh.boilerplate.constant.DomainConstant.USER_DELETED_SUCCESSFULLY;
import static com.hqh.boilerplate.constant.MessageTypeConstant.MESSAGE_SUCCESS;
import static com.hqh.boilerplate.utils.ResponseUtils.response;
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
