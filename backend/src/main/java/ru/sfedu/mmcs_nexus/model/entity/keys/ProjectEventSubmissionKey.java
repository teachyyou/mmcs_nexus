package ru.sfedu.mmcs_nexus.model.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class ProjectEventSubmissionKey implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "event_id")
    private UUID eventId;

    public ProjectEventSubmissionKey() {
    }

    public ProjectEventSubmissionKey(UUID projectId, UUID eventId) {
        this.projectId = projectId;
        this.eventId = eventId;
    }
}