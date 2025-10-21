package ru.sfedu.mmcs_nexus.model.payload.user;

import ru.sfedu.mmcs_nexus.validators.UserEmail;
import ru.sfedu.mmcs_nexus.validators.UserName;

public class UpdateProfileRequestPayload {
    @UserName
    private String firstName;

    @UserName
    private String lastName;

    @UserEmail
    private String email;

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
}
