package com.hqh.boilerplate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.hqh.boilerplate.constant.PatternConstant.EMAIL_PATTERN;
import static com.hqh.boilerplate.constant.PatternConstant.NAME_PATTERN;
import static com.hqh.boilerplate.constant.PatternConstant.USERNAME_PATTERN;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRequestDTO {
    @NotBlank(groups = OnUpdate.class)
    @Pattern(regexp = USERNAME_PATTERN)
    private String currentUsername;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = NAME_PATTERN)
    private String firstName;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = NAME_PATTERN)
    private String lastName;

    @NotBlank
    @Pattern(regexp = USERNAME_PATTERN)
    private String username;

    @NotBlank
    @Email
    @Size(max = 50)
    @Pattern(regexp = EMAIL_PATTERN)
    private String email;

    @NotBlank
    @Pattern(regexp = "ROLE_USER|ROLE_ADMIN|ROLE_TEACHER",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String roles;

    @NotNull
    @JsonProperty("isActive")
    private Boolean active;

    @NotNull
    @JsonProperty("isNonLocked")
    private Boolean nonLocked;
}
