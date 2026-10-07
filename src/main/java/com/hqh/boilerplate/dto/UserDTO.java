package com.hqh.boilerplate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hqh.boilerplate.enumeration.AuthenticationType;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private Timestamp dateOfBirth;
    private String profileImageUrl;
    private Date lastLogin;
    private Date joinDate;
    private String roles;
    private AuthenticationType authType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @JsonProperty("isActive")
    private boolean active;

    @JsonProperty("isNonLocked")
    private boolean nonLocked;

}
