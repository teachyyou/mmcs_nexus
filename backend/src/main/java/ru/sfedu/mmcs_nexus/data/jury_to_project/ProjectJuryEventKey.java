package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class ProjectJuryEventKey implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "jury_id")
    private UUID juryId;

    @Column(name = "event_id")
    private UUID eventId;

    // Конструкторы
    public ProjectJuryEventKey() {
    }

    public ProjectJuryEventKey(UUID projectId, UUID juryId, UUID eventId) {
        this.projectId = projectId;
        this.juryId = juryId;
        this.eventId = eventId;
    }

    // Геттеры и сеттеры

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getJuryId() {
        return juryId;
    }

    public void setJuryId(UUID juryId) {
        this.juryId = juryId;
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

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
}
