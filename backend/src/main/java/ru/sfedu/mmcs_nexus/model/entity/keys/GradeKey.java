package ru.sfedu.mmcs_nexus.model.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
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


}
