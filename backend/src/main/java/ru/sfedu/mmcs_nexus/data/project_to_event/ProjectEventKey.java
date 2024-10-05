package ru.sfedu.mmcs_nexus.data.project_to_event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

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
