package ru.sfedu.mmcs_nexus.model.payload.user;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.validators.UserEmail;
import ru.sfedu.mmcs_nexus.validators.UserName;

@Setter
@Getter
public class UpdateProfileRequestPayload {
    @UserName
    private String firstName;

    @UserName
    private String lastName;

    @UserEmail
    private String email;

}
