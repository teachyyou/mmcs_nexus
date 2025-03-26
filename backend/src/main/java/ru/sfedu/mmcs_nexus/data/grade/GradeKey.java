package ru.sfedu.mmcs_nexus.data.grade;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class GradeKey implements Serializable {

    @Column(name = "project_id")
    @NotNull(message = "projectId cannot be null")
    private UUID projectId;

    @Column(name = "event_id")
    @NotNull(message = "eventId cannot be null")
    private UUID eventId;

    @Column(name = "jury_id")
    @NotNull(message = "juryId cannot be null")
    private UUID juryId;

    public GradeKey(UUID projectId, UUID eventId, UUID juryId) {
        this.projectId=projectId;
        this.eventId=eventId;
        this.juryId=juryId;
    }

    public GradeKey() {

    }


    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getJuryId() {
        return juryId;
    }

    public void setJuryId(UUID juryId) {
        this.juryId = juryId;
    }
}
