package ru.sfedu.mmcs_nexus.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyTakenException extends RuntimeException{

    public EmailAlreadyTakenException(String email) {
        super(STR."Email is already taken: \{email}");
    }


}
