package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;

import java.util.UUID;

@Getter
public class ProjectDTO {

    private final UUID id;
    private final Integer externalId;
    private final Integer quantityOfStudents;
    private final String captainName;
    private final UUID captainUserId;
    private final String captainUserFullName;
    private final String captainLogin;
    private final boolean hasCaptain;
    private final boolean full;
    private final String track;
    private final String technologies;
    private final String name;
    private final String description;
    private final String type;
    private final int year;

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.externalId = project.getExternalId();
        this.quantityOfStudents = project.getQuantityOfStudents();
        this.captainName = project.getCaptainName();
        this.full = project.isFull();
        this.track = project.getTrack();
        this.technologies = project.getTechnologies();
        this.name = project.getName();
        this.description = project.getDescription();
        this.type = project.getType();
        this.year = project.getYear();

        User captain = project.getCaptain();

        this.hasCaptain = captain != null;
        this.captainUserId = captain != null ? captain.getId() : null;
        this.captainLogin = captain != null ? captain.getLogin() : null;
        this.captainUserFullName = captain != null ? buildFullName(captain) : null;
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? user.getLogin() : fullName;
    }
}