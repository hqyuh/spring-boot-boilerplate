package com.hqh.quizserver.exceptions.domain.user;

public class UsernameOrEmailExistException extends Exception {
    public UsernameOrEmailExistException(String message) {
        super(message);
    }
}
