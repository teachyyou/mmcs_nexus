package ru.sfedu.mmcs_nexus.model.payload.admin;


import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.validators.UserEmail;
import ru.sfedu.mmcs_nexus.validators.UserName;

@Setter
@Getter
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

}
