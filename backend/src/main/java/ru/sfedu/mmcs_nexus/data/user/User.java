package ru.sfedu.mmcs_nexus.data.user;

import jakarta.persistence.*;
import lombok.Data;

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
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    public User() {
        this.status = UserStatus.NON_VERIFIED;
    }

    public User(UUID id, String firstName, String lastName, String login, int group, int course, UserStatus status, UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.userGroup = group;
        this.userCourse = course;
        this.status = status;
        this.role = role;
    }

    public User(String firstName, String lastName, String login, int group, int course, UserStatus status, UserRole role) {
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
        this.status = UserStatus.NON_VERIFIED;
        this.role = UserRole.ROLE_USER;
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role)  {
        this.role = role;
    }

    public void verifyExistingUser(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setUserGroup(user.getUserGroup());
        setUserCourse(user.getUserCourse());
        setStatus(UserStatus.VERIFIED);
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
