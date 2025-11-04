package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;
    private String firstName;
    private String lastName;
    private String login;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserEnums.UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserEnums.UserRole role;


    public User() {
        this.status = UserEnums.UserStatus.NON_VERIFIED;
    }

    public User(UUID id, String firstName, String lastName, String login, String email, UserEnums.UserStatus status, UserEnums.UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.email = email;
        this.status = status;
        this.role = role;
    }

    public User(String firstName, String lastName, String login, String email, UserEnums.UserStatus status, UserEnums.UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.email = email;
        this.status = status;
        this.role = role;
    }

    public User(String login) {
        this.login = login;
        this.status = UserEnums.UserStatus.NON_VERIFIED;
        this.role = UserEnums.UserRole.ROLE_USER;
    }

}
