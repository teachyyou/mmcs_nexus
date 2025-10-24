package ru.sfedu.mmcs_nexus.model.internal;

public record ImportRowIssue(long rowNumber, String code, String message) {

    public static ImportRowIssue of(long rowNumber, String code, String message) {
        return new ImportRowIssue(rowNumber, code, message);
    }
}