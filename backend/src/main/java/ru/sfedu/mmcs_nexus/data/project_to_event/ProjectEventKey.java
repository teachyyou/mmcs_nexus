package ru.sfedu.mmcs_nexus.data.project_to_event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ProjectEventKey implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "event_id")
    private Long eventId;

    public ProjectEventKey() {
    }

    public ProjectEventKey(Long projectId, Long eventId) {
        this.projectId = projectId;
        this.eventId = eventId;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
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
