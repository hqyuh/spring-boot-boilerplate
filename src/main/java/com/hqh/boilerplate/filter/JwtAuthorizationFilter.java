package com.hqh.boilerplate.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.hqh.boilerplate.entity.User;
import com.hqh.boilerplate.entity.UserPrincipal;
import com.hqh.boilerplate.repository.UserRepository;
import com.hqh.boilerplate.utility.JWTTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hqh.boilerplate.constant.SecurityConstant.OPTIONS_HTTP_METHOD;
import static com.hqh.boilerplate.constant.SecurityConstant.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider,
                                  UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
            response.setStatus(OK.value());
        } else {
            authenticate(request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return;
        }

        try {
            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            String username = jwtTokenProvider.getSubject(token);
            if (!jwtTokenProvider.isTokenValid(username, token)
                    || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            User user = userRepository.findUserByUsername(username);
            if (user == null || user.getRoles() == null) {
                SecurityContextHolder.clearContext();
                return;
            }

            UserPrincipal principal = new UserPrincipal(user);
            if (!principal.isEnabled() || !principal.isAccountNonLocked()) {
                SecurityContextHolder.clearContext();
                return;
            }

            List<GrantedAuthority> authorities = new ArrayList<>(principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(
                    jwtTokenProvider.getAuthentication(username, authorities, request));
        } catch (JWTVerificationException ex) {
            SecurityContextHolder.clearContext();
        }
    }
}
