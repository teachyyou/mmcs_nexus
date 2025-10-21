package ru.sfedu.mmcs_nexus.exceptions;

public class EmailAlreadyTakenException extends RuntimeException{
    public EmailAlreadyTakenException(String email) {
        super(STR."Email is already taken: \{email}");
    }

}
