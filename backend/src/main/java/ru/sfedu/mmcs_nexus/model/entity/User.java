package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    private String firstName;
    private String lastName;
    private String login;
    private int userGroup;
    
    private int userCourse;
    
    @Enumerated(EnumType.STRING)
    private UserEnums.UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserEnums.UserRole role;


    public User() {
        this.status = UserEnums.UserStatus.NON_VERIFIED;
    }

    public User(UUID id, String firstName, String lastName, String login, int group, int course, UserEnums.UserStatus status, UserEnums.UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.userGroup = group;
        this.userCourse = course;
        this.status = status;
        this.role = role;
    }

    public User(String firstName, String lastName, String login, int group, int course, UserEnums.UserStatus status, UserEnums.UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.userGroup = group;
        this.userCourse = course;
        this.status = status;
        this.role = role;
    }

    public User(String login) {
        this.login = login;
        this.status = UserEnums.UserStatus.NON_VERIFIED;
        this.role = UserEnums.UserRole.ROLE_USER;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return STR."\{this.firstName} \{this.lastName}";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(int userGroup) {
        this.userGroup = userGroup;
    }

    public int getUserCourse() {
        return userCourse;
    }

    public void setUserCourse(int userCourse) {
        this.userCourse = userCourse;
    }

    public UserEnums.UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserEnums.UserStatus status) {
        this.status = status;
    }

    public UserEnums.UserRole getRole() {
        return role;
    }

    public void setRole(UserEnums.UserRole role)  {
        this.role = role;
    }

    public void verifyExistingUser(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setUserGroup(user.getUserGroup());
        setUserCourse(user.getUserCourse());
        setStatus(UserEnums.UserStatus.VERIFIED);
    }

    public void editExistingUser(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setUserGroup(user.getUserGroup());
        setUserCourse(user.getUserCourse());
        setStatus(user.getStatus());
        setRole(user.getRole());
    }

}
