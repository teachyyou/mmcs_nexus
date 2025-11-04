package ru.sfedu.mmcs_nexus.exceptions;

public class WrongRequestParamException extends RuntimeException {
    public WrongRequestParamException(String message) {
        super(message);
    }
}
