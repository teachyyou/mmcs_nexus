package ru.sfedu.mmcs_nexus.model.enums.entity;

public class UserEnums {
    public enum UserRole {
        ROLE_GUEST,
        ROLE_USER,
        ROLE_JURY,
        ROLE_ADMIN
    }

    public enum UserStatus {
        NON_VERIFIED,
        VERIFIED,
        BLOCKED
    }
}
