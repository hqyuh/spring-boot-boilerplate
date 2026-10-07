package com.hqh.quizserver.exceptions;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.hqh.quizserver.entity.ApiResponse;
import com.hqh.quizserver.exceptions.domain.user.UserNotFoundException;
import com.hqh.quizserver.exceptions.domain.user.UsernameOrEmailExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.NoResultException;
import tools.jackson.databind.DatabindException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.hqh.quizserver.constant.DomainConstant.*;
import static com.hqh.quizserver.constant.MessageTypeConstant.MESSAGE_ERROR;
import static org.springframework.http.HttpStatus.*;

// exception api
@RestControllerAdvice
public class ExceptionHandling {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public ResponseEntity<ApiResponse> createHttpResponse(HttpStatus httpStatus,
                                                          String type,
                                                          String message) {
        return new ResponseEntity<>(new ApiResponse(httpStatus.value(),
                httpStatus,
                type.toUpperCase(),
                httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase()), httpStatus);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse> accountDisabledException() {
        return createHttpResponse(BAD_REQUEST, MESSAGE_ERROR, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> badCredentialsException() {
        return createHttpResponse(BAD_REQUEST, MESSAGE_ERROR, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> accessDeniedException() {
        return createHttpResponse(FORBIDDEN, MESSAGE_ERROR, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse> lockedException() {
        return createHttpResponse(UNAUTHORIZED, MESSAGE_ERROR, ACCOUNT_LOCKER);
    }

    /***
     * token expires
     *
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse> tokenExpiredException(TokenExpiredException exception) {
        return createHttpResponse(UNAUTHORIZED, MESSAGE_ERROR, exception.getMessage());
    }

    @ExceptionHandler(UsernameOrEmailExistException.class)
    public ResponseEntity<ApiResponse> usernameOrEmailExistException(UsernameOrEmailExistException exception) {
        return createHttpResponse(BAD_REQUEST, MESSAGE_ERROR, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, MESSAGE_ERROR, exception.getMessage());
    }

    /***
     * introduce another method
     *
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods())
                                            .iterator()
                                            .next();
        return createHttpResponse(METHOD_NOT_ALLOWED, MESSAGE_ERROR, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> noResourceFoundException(NoResourceFoundException exception) {
        return createHttpResponse(NOT_FOUND, MESSAGE_ERROR, NO_MAPPING_FOR_URL);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> internalServerErrorException(Exception exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, MESSAGE_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<ApiResponse> notFoundException(NoResultException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, MESSAGE_ERROR, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse> iOException(IOException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, MESSAGE_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

//    @ExceptionHandler(NoHandlerFoundException.class)
//    public ResponseEntity<HttpResponse> methodNotSupportedException(NoHandlerFoundException exception) {
//        return createHttpResponse(BAD_REQUEST, "This page was not found");
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        List<FieldViolation> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        return fieldErrors(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> unreadable(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();
        if (cause instanceof DatabindException mapping && !mapping.getPath().isEmpty()) {
            String field = mapping.getPath().get(mapping.getPath().size() - 1).getPropertyName();
            if (field != null) {
                return fieldErrors(List.of(new FieldViolation(field, "Invalid value")));
            }
        }
        return createHttpResponse(BAD_REQUEST, MESSAGE_ERROR, "INVALID REQUEST BODY");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> missingParameter(MissingServletRequestParameterException exception) {
        return fieldErrors(List.of(new FieldViolation(exception.getParameterName(), "is mandatory")));
    }

    private ResponseEntity<ApiResponse> fieldErrors(List<FieldViolation> errors) {
        ApiResponse body = new ApiResponse(BAD_REQUEST.value(), BAD_REQUEST,
                MESSAGE_ERROR.toUpperCase(), BAD_REQUEST.getReasonPhrase().toUpperCase(), errors);
        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    private record FieldViolation(String field, String message) {
    }

}
