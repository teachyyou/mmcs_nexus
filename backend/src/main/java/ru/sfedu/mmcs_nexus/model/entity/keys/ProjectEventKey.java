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
public class ProjectEventKey implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "event_id")
    private UUID eventId;

    public ProjectEventKey() {
    }

    public ProjectEventKey(UUID projectId, UUID eventId) {
        this.projectId = projectId;
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectEventKey that = (ProjectEventKey) o;

        if (!projectId.equals(that.projectId)) return false;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        int result = projectId.hashCode();
        result = 31 * result + eventId.hashCode();
        return result;
    }
}
