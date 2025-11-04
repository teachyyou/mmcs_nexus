package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.UUID;

@Setter
@Getter
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String login;
    private String email;
    private UserEnums.UserRole role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.login = user.getLogin();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

}
