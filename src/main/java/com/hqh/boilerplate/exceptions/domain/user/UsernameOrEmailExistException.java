package com.hqh.boilerplate.exceptions.domain.user;

public class UsernameOrEmailExistException extends Exception {
    public UsernameOrEmailExistException(String message) {
        super(message);
    }
}
