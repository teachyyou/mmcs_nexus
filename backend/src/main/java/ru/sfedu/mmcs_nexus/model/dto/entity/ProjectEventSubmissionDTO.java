package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProjectEventSubmissionDTO {

    private final UUID projectId;
    private final UUID eventId;
    private final String presentationUrl;
    private final String repositoryUrl;
    private final String releaseUrl;
    private final String comment;
    private final UUID submittedById;
    private final String submittedByLogin;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProjectEventSubmissionDTO(ProjectEventSubmission submission) {
        this.projectId = submission.getProject().getId();
        this.eventId = submission.getEvent().getId();
        this.presentationUrl = submission.getPresentationUrl();
        this.repositoryUrl = submission.getRepositoryUrl();
        this.releaseUrl = submission.getReleaseUrl();
        this.comment = submission.getComment();
        this.submittedById = submission.getSubmittedBy().getId();
        this.submittedByLogin = submission.getSubmittedBy().getLogin();
        this.createdAt = submission.getCreatedAt();
        this.updatedAt = submission.getUpdatedAt();
    }
}