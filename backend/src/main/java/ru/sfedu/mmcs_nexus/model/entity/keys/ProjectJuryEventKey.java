package ru.sfedu.mmcs_nexus.model.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Embeddable
public class ProjectJuryEventKey implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "jury_id")
    private UUID juryId;

    @Column(name = "event_id")
    private UUID eventId;

    public ProjectJuryEventKey() {
    }

    public ProjectJuryEventKey(UUID projectId, UUID juryId, UUID eventId) {
        this.projectId = projectId;
        this.juryId = juryId;
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectJuryEventKey that = (ProjectJuryEventKey) o;

        if (!projectId.equals(that.projectId) || !eventId.equals(that.eventId)) return false;
        return juryId.equals(that.juryId);
    }

    @Override
    public int hashCode() {
        int result = projectId.hashCode();
        result = 31 * result + juryId.hashCode();
        result = 17 * result + eventId.hashCode();
        return result;
    }

}
