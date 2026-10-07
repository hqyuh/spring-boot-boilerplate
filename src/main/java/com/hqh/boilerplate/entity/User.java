package com.hqh.boilerplate.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hqh.boilerplate.entity.base.BaseEntity;
import com.hqh.boilerplate.enumeration.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@Entity
public class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "password", length = 64, nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "phone_number", length = 12)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private Timestamp dateOfBirth;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "last_login_date_display")
    private Date lastLoginDateDisplay;

    @Column(name = "join_date")
    private Date joinDate;

    private String roles;

    private String[] authorities;
    private boolean isActive;
    private boolean isNotLocked;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 25)
    private AuthenticationType authType;

    public User(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
