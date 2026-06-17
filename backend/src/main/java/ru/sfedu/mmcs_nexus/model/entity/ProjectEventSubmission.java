package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventSubmissionKey;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_event_submissions")
@Getter
@Setter
public class ProjectEventSubmission {

    @EmbeddedId
    private ProjectEventSubmissionKey id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @MapsId("eventId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "presentation_url", length = 1024)
    private String presentationUrl;

    @Column(name = "repository_url", length = 1024)
    private String repositoryUrl;

    @Column(name = "release_url", length = 1024)
    private String releaseUrl;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_uuid", nullable = false)
    private User submittedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ProjectEventSubmission() {
    }

    public ProjectEventSubmission(Project project, Event event, User submittedBy) {
        this.id = new ProjectEventSubmissionKey(project.getId(), event.getId());
        this.project = project;
        this.event = event;
        this.submittedBy = submittedBy;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}