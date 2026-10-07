package com.hqh.boilerplate.enumeration;


import static com.hqh.boilerplate.constant.Authority.*;

public enum Role {

    ROLE_USER(USER_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES),
    ROLE_TEACHER(TEACHER_AUTHORITIES);

    private final String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }

}
