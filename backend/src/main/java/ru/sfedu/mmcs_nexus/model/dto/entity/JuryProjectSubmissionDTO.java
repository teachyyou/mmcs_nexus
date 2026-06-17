package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class JuryProjectSubmissionDTO {

    private final UUID projectId;
    private final String projectName;
    private final Integer projectExternalId;
    private final String projectTrack;
    private final String projectTechnologies;
    private final UUID captainId;
    private final String captainFullName;
    private final String captainLogin;
    private final boolean submitted;
    private final String presentationUrl;
    private final String repositoryUrl;
    private final String releaseUrl;
    private final String comment;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public JuryProjectSubmissionDTO(Project project, ProjectEventSubmission submission) {
        this.projectId = project.getId();
        this.projectName = project.getName();
        this.projectExternalId = project.getExternalId();
        this.projectTrack = project.getTrack();
        this.projectTechnologies = project.getTechnologies();

        User captain = project.getCaptain();

        this.captainId = captain != null ? captain.getId() : null;
        this.captainFullName = captain != null ? buildFullName(captain) : null;
        this.captainLogin = captain != null ? captain.getLogin() : null;

        this.submitted = submission != null;
        this.presentationUrl = submission != null ? submission.getPresentationUrl() : null;
        this.repositoryUrl = submission != null ? submission.getRepositoryUrl() : null;
        this.releaseUrl = submission != null ? submission.getReleaseUrl() : null;
        this.comment = submission != null ? submission.getComment() : null;
        this.createdAt = submission != null ? submission.getCreatedAt() : null;
        this.updatedAt = submission != null ? submission.getUpdatedAt() : null;
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? user.getLogin() : fullName;
    }
}