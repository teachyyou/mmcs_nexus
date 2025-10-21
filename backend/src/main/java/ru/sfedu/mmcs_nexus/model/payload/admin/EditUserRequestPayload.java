package ru.sfedu.mmcs_nexus.model.payload.admin;


import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.validators.UserEmail;
import ru.sfedu.mmcs_nexus.validators.UserName;

public class EditUserRequestPayload {
    @UserName
    private String firstName;
    @UserName
    private String lastName;
    @UserEmail
    private String email;

    private UserEnums.UserRole role;

    private UserEnums.UserStatus status;

    public EditUserRequestPayload() {}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserEnums.UserRole getRole() {
        return role;
    }

    public void setRole(UserEnums.UserRole role) {
        this.role = role;
    }

    public UserEnums.UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserEnums.UserStatus status) {
        this.status = status;
    }
}
