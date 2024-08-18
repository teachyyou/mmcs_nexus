package ru.sfedu.mmcs_nexus.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    public enum UserStatus {
        NON_VERIFIED,
        VERIFIED,
        BLOCKED
    }

    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    private String firstName;
    private String lastName;
    private String login;
    private int userGroup;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public User() {
        this.status = UserStatus.NON_VERIFIED; // Default status
    }

    public User(Long id, String firstName, String lastName, String login, int userGroup, UserStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.userGroup = userGroup;
        this.status = status;
    }

    public User(String firstName, String lastName, String login, int userGroup, UserStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.userGroup = userGroup;
        this.status = status;
    }

    public User(String login) {
        this.login = login;
        this.status = UserStatus.NON_VERIFIED;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void verifyExistingUser(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setUserGroup(user.getUserGroup());
        setStatus(UserStatus.VERIFIED);
    }
}
