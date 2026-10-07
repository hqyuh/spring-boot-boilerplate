package com.hqh.quizserver.services.impl;

import com.hqh.quizserver.dto.UserDTO;
import com.hqh.quizserver.dto.UserRegisterRequestDTO;
import com.hqh.quizserver.dto.UserRequestDTO;
import com.hqh.quizserver.entity.User;
import com.hqh.quizserver.entity.UserPrincipal;
import com.hqh.quizserver.enumeration.Role;
import com.hqh.quizserver.exceptions.domain.user.UserNotFoundException;
import com.hqh.quizserver.exceptions.domain.user.UsernameOrEmailExistException;
import com.hqh.quizserver.mapper.UserMapper;
import com.hqh.quizserver.repository.UserRepository;
import com.hqh.quizserver.services.LoginAttemptService;
import com.hqh.quizserver.services.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hqh.quizserver.constant.UserImplConstant.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Transactional
public class UserServiceImpl implements UserDetailsService, UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           LoginAttemptService loginAttemptService,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.userMapper = userMapper;
    }

    /**
     * This function will check the user is in the database,
     * if successful it will return the userPrincipal to Spring Security
     * */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email);

        if(user == null) {
            log.error("No user found by email: {}", email);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        } else {
            //
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLogin());
            user.setLastLogin(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);

            log.info("Returning found user by email: {}", email);

            return userPrincipal;
        }
    }

    /***
     * isNotLocked() -> function that checks if there is a lock
     * If the account is not locked, check the number of logins
     *
     * @param user object user
     */
    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()) {
            user.setNotLocked(!loginAttemptService.hasExceededMaxAttempts(user.getEmail()));
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getEmail());
        }
    }



    @Override
    public UserDTO register(UserRegisterRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        User user = new User();
        validateNewUsernameAndEmail(EMPTY, request.getUsername(), request.getEmail());
        setUserInformation(user, request.getFirstName(), request.getLastName(),
                request.getUsername(), request.getEmail(), request.getRoles());
        user.setPassword(encodePassword(request.getPassword()));
        user.setCreatedBy("Self-registered users");
        user.setUpdatedBy("None");
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    /**
     * It validates the new username and email, sets the user's first name, last name, username, email, join date, active
     * status, locked status, role, authorities, profile image url, created at, and updated at
     *
     * @param user The user object that will be updated.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param username The username of the user.
     * @param email The email address of the user.
     * @param role The role of the user.
     */
    public void setUserInformation(User user, String firstName, String lastName, String username, String email, String role)
            throws UserNotFoundException, UsernameOrEmailExistException {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRoles(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String getTemporaryProfileImageUrl(String username) {
        // http://localhost:8081
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path("/user/profile/" + username)
                                          .toUriString();
    }


    /**
     * <h2>It checks if the new username and email are valid and if they are, it returns the current user</h2>
     *
     * @param currentUsername the username of the user who is currently logged in.
     * @param newUsername the new username that the user wants to change to
     * @param newEmail the new email address that the user wants to change to
     * @return The current user is being returned.
     */
    private User validateNewUsernameAndEmail(String currentUsername,
                                             String newUsername,
                                             String newEmail)
            throws UsernameOrEmailExistException, UserNotFoundException {

        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);

        // check if not null
        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);

            if (currentUser == null) {
                log.error("No user found by username {}", currentUsername);
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }
            // if the user's new name is not null and exists in the database
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                log.error("Username already exists");
                throw new UsernameOrEmailExistException(USERNAME_OR_EMAIL_ALREADY_EXISTS);
            }
            // if the user's email is not empty and exists in the database
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                log.error("Email already exists");
                throw new UsernameOrEmailExistException(USERNAME_OR_EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUsername != null) {
                log.error("Username already exists");
                throw new UsernameOrEmailExistException(USERNAME_OR_EMAIL_ALREADY_EXISTS);
            }
            if (userByNewEmail != null) {
                log.error("Email already exists");
                throw new UsernameOrEmailExistException(USERNAME_OR_EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    /***
     * create Random password with 8 character
     *
     * @return password
     */
    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }


    @Override
    public UserDTO addNewUser(UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        User user = new User();
        validateNewUsernameAndEmail(EMPTY, request.getUsername(), request.getEmail());
        setUserInformation(user, request.getFirstName(), request.getLastName(),
                request.getUsername(), request.getEmail(), request.getRoles());
        user.setActive(request.getActive());
        user.setNotLocked(request.getNonLocked());
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        user.setCreatedBy(getCurrentUser().getUsername());
        user.setUpdatedBy(getCurrentUser().getUsername());
        userRepository.save(user);
        UserDTO response = userMapper.toUserResponseDto(user);
        response.setPassword(password);
        return response;
    }

    @Override
    public UserDTO updateUser(UserRequestDTO request)
            throws UserNotFoundException, UsernameOrEmailExistException {
        User currentUser = validateNewUsernameAndEmail(
                request.getCurrentUsername(), request.getUsername(), request.getEmail());
        if (currentUser != null) {
            setUserInformation(currentUser, request.getFirstName(), request.getLastName(),
                    request.getUsername(), request.getEmail(), request.getRoles());
            currentUser.setActive(request.getActive());
            currentUser.setNotLocked(request.getNonLocked());
            currentUser.setCreatedBy(getCurrentUser().getUsername());
            currentUser.setUpdatedBy(getCurrentUser().getUsername());
            userRepository.save(currentUser);
        }
        return userMapper.toUserResponseDto(currentUser);
    }

    @Override
    public void deleteUser(Long id) throws UserNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(NO_USER_FOUND_BY_ID + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO findUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new UserNotFoundException(NO_USER_FOUND_BY_ID + id);
        }
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public User getCurrentUser() {
        String userPrincipal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByUsername(userPrincipal);
    }
}
